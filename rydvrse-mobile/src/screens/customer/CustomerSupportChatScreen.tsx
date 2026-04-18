import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  FlatList,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  StyleSheet,
  View,
} from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { ScreenHeader } from "@/components/layout/ScreenHeader";
import { Screen } from "@/components/layout/Screen";
import { TextField } from "@/components/forms/TextField";
import { SUPPORT_FAQS } from "@/constants/supportFaq";
import { colors, radius, semantic, space, spacing } from "@/theme";

type ChatMessage = {
  id: string;
  author: "user" | "system" | "agent";
  body: string;
  timestamp: string;
};

function formatTime(iso: string) {
  const date = new Date(iso);
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${hours}:${minutes}`;
}

function composeAgentReply(faqId?: string, faqAnswer?: string) {
  if (faqAnswer) {
    return `${faqAnswer}\n\nIs there anything else I can help you with, or would you like me to escalate this to a specialist?`;
  }
  if (faqId === "custom") {
    return "Thanks for reaching out. Could you share the trip ID or a short description so I can pull up the right details?";
  }
  return "Thanks for reaching out — a specialist will follow up here in a moment. Feel free to share any additional detail in the meantime.";
}

/**
 * Support chat shown when the user taps an FAQ or the generic "Chat with us"
 * entry. The flow is purely simulated for the test-mode build:
 * 1. The user's question appears immediately.
 * 2. A system message confirms the request is queued.
 * 3. After ~3 seconds an agent "joins" with a canned, FAQ-aware reply.
 * 4. Further user messages receive short acknowledgements.
 */
export function CustomerSupportChatScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const insets = useSafeAreaInsets();
  const listRef = useRef<FlatList<ChatMessage>>(null);
  const seedQuestion: string | undefined = route.params?.question;
  const seedFaqId: string | undefined = route.params?.faqId;

  const faq = useMemo(() => SUPPORT_FAQS.find((item) => item.id === seedFaqId), [seedFaqId]);

  const [messages, setMessages] = useState<ChatMessage[]>(() => {
    const now = new Date();
    const base: ChatMessage[] = [
      {
        id: "system-welcome",
        author: "system",
        body: "You are in the Rydvrse support queue. Most customers wait under 2 minutes.",
        timestamp: now.toISOString(),
      },
    ];
    if (seedQuestion) {
      base.push({
        id: "user-seed",
        author: "user",
        body: seedQuestion,
        timestamp: new Date(now.getTime() + 100).toISOString(),
      });
    }
    return base;
  });
  const [draft, setDraft] = useState("");
  const [agentConnected, setAgentConnected] = useState(false);
  const [typing, setTyping] = useState(true);

  useEffect(() => {
    if (!seedQuestion) {
      setTyping(false);
      return;
    }
    const connectTimer = setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          id: "system-joined",
          author: "system",
          body: "Riya from Rydvrse Support joined the chat.",
          timestamp: new Date().toISOString(),
        },
      ]);
      setAgentConnected(true);
    }, 1600);

    const replyTimer = setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          id: "agent-initial",
          author: "agent",
          body: composeAgentReply(faq?.id, faq?.answer),
          timestamp: new Date().toISOString(),
        },
      ]);
      setTyping(false);
    }, 3200);

    return () => {
      clearTimeout(connectTimer);
      clearTimeout(replyTimer);
    };
  }, [faq?.answer, faq?.id, seedQuestion]);

  useEffect(() => {
    if (messages.length === 0) {
      return;
    }
    const timer = setTimeout(() => {
      listRef.current?.scrollToEnd({ animated: true });
    }, 80);
    return () => clearTimeout(timer);
  }, [messages.length, typing]);

  const sendDraft = useCallback(() => {
    const trimmed = draft.trim();
    if (!trimmed) {
      return;
    }
    setDraft("");
    setMessages((prev) => [
      ...prev,
      {
        id: `user-${Date.now()}`,
        author: "user",
        body: trimmed,
        timestamp: new Date().toISOString(),
      },
    ]);
    if (!agentConnected) {
      setTyping(true);
      setTimeout(() => {
        setMessages((prev) => [
          ...prev,
          {
            id: "system-joined-late",
            author: "system",
            body: "Riya from Rydvrse Support joined the chat.",
            timestamp: new Date().toISOString(),
          },
        ]);
        setAgentConnected(true);
      }, 1200);
    } else {
      setTyping(true);
    }
    setTimeout(() => {
      setMessages((prev) => [
        ...prev,
        {
          id: `agent-${Date.now()}`,
          author: "agent",
          body:
            "Thanks for sharing that. I have logged it against your active trip — you'll see an update shortly, and we stay on this chat until it's resolved.",
          timestamp: new Date().toISOString(),
        },
      ]);
      setTyping(false);
    }, 2400);
  }, [agentConnected, draft]);

  return (
    <Screen scrollable={false}>
      <ScreenHeader
        title={faq?.question ? "Support chat" : "Chat with Rydvrse"}
        subtitle={agentConnected ? "Agent is online" : typing ? "Connecting you to an agent…" : "Send a message to get started"}
      />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === "ios" ? "padding" : undefined}
        keyboardVerticalOffset={Platform.OS === "ios" ? 12 : 0}
      >
        <FlatList
          ref={listRef}
          data={messages}
          keyExtractor={(item) => item.id}
          contentContainerStyle={[styles.listContent, { paddingBottom: 12 }]}
          showsVerticalScrollIndicator={false}
          renderItem={({ item }) => {
            if (item.author === "system") {
              return (
                <View style={styles.systemRow}>
                  <AppText variant="caption" style={styles.systemText}>
                    {item.body}
                  </AppText>
                </View>
              );
            }
            const isUser = item.author === "user";
            return (
              <View style={[styles.bubbleRow, isUser ? styles.bubbleRowRight : styles.bubbleRowLeft]}>
                {!isUser ? (
                  <View style={styles.avatar}>
                    <AppIcon name="profile" size={14} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
                  </View>
                ) : null}
                <View style={[styles.bubble, isUser ? styles.bubbleUser : styles.bubbleAgent]}>
                  <AppText variant="body" style={isUser ? styles.bubbleTextUser : styles.bubbleTextAgent}>
                    {item.body}
                  </AppText>
                  <AppText variant="caption" style={isUser ? styles.bubbleMetaUser : styles.bubbleMetaAgent}>
                    {formatTime(item.timestamp)}
                  </AppText>
                </View>
              </View>
            );
          }}
          ListFooterComponent={
            typing ? (
              <View style={[styles.bubbleRow, styles.bubbleRowLeft]}>
                <View style={styles.avatar}>
                  <AppIcon name="profile" size={14} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
                </View>
                <View style={[styles.bubble, styles.bubbleAgent, styles.typingBubble]}>
                  <View style={styles.typingDot} />
                  <View style={[styles.typingDot, styles.typingDotMid]} />
                  <View style={styles.typingDot} />
                </View>
              </View>
            ) : null
          }
        />
        <View style={[styles.composer, { paddingBottom: Math.max(insets.bottom, 12) }]}>
          <View style={styles.composerField}>
            <TextField
              label="Message"
              placeholder="Type your message"
              value={draft}
              onChangeText={setDraft}
              icon="document"
            />
          </View>
          <Pressable
            onPress={sendDraft}
            accessibilityRole="button"
            accessibilityLabel="Send message"
            style={({ pressed }) => [styles.sendButton, pressed && { opacity: 0.85 }]}
          >
            <AppIcon name="check" size={18} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  flex: {
    flex: 1,
  },
  listContent: {
    paddingVertical: space[2],
    gap: space[2],
  },
  systemRow: {
    alignItems: "center",
    paddingVertical: space[1],
  },
  systemText: {
    color: semantic.text.secondary,
    textAlign: "center",
  },
  bubbleRow: {
    flexDirection: "row",
    alignItems: "flex-end",
    gap: space[2],
  },
  bubbleRowLeft: {
    justifyContent: "flex-start",
  },
  bubbleRowRight: {
    justifyContent: "flex-end",
  },
  avatar: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: colors.brand.strong,
    alignItems: "center",
    justifyContent: "center",
  },
  bubble: {
    maxWidth: "78%",
    borderRadius: radius.xl,
    paddingVertical: space[2],
    paddingHorizontal: space[3],
    gap: 4,
  },
  bubbleUser: {
    backgroundColor: colors.brand.primary,
    borderBottomRightRadius: 6,
  },
  bubbleAgent: {
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    borderBottomLeftRadius: 6,
  },
  bubbleTextUser: {
    color: semantic.text.onBrand,
  },
  bubbleTextAgent: {
    color: semantic.text.primary,
  },
  bubbleMetaUser: {
    color: semantic.text.onBrand,
    opacity: 0.8,
    textAlign: "right",
  },
  bubbleMetaAgent: {
    color: semantic.text.secondary,
    textAlign: "left",
  },
  typingBubble: {
    flexDirection: "row",
    alignItems: "center",
    gap: 4,
    paddingVertical: space[2],
  },
  typingDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: semantic.text.secondary,
    opacity: 0.6,
  },
  typingDotMid: {
    opacity: 0.9,
  },
  composer: {
    flexDirection: "row",
    alignItems: "flex-end",
    gap: spacing.sm,
    paddingTop: space[2],
    borderTopWidth: 1,
    borderTopColor: semantic.border.soft,
  },
  composerField: {
    flex: 1,
  },
  sendButton: {
    width: 46,
    height: 46,
    borderRadius: 23,
    backgroundColor: colors.brand.primary,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 4,
  },
});
