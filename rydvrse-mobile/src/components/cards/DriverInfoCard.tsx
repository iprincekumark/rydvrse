import React from "react";
import { StyleSheet, View } from "react-native";

import { Avatar } from "@/components/primitives/Avatar";
import { Chip } from "@/components/primitives/Chip";
import { IconButton } from "@/components/primitives/IconButton";
import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, shadows, space } from "@/theme";

type DriverInfoCardProps = {
  name: string;
  rating: number | string;
  vehicle?: string;
  languages?: string;
  eta?: string;
  verificationBadge?: string;
  onCall?: () => void;
  onChat?: () => void;
};

export function DriverInfoCard({
  name,
  rating,
  vehicle,
  languages,
  eta,
  verificationBadge,
  onCall,
  onChat,
}: DriverInfoCardProps) {
  return (
    <View style={styles.card}>
      <View style={styles.top}>
        <Avatar name={name} size={56} />
        <View style={styles.info}>
          <Text variant="headingSm">{name}</Text>
          <View style={styles.badges}>
            <Chip label={`★ ${rating}`} tone="success" />
            {verificationBadge ? <Chip label={verificationBadge} tone="info" /> : null}
          </View>
          {vehicle ? <Text variant="bodySm" color={semantic.text.muted}>{vehicle}</Text> : null}
        </View>
        <View style={styles.actions}>
          {onCall ? <IconButton icon="phone" accessibilityLabel="Call driver" onPress={onCall} /> : null}
          {onChat ? <IconButton icon="help" accessibilityLabel="Chat with driver" onPress={onChat} /> : null}
        </View>
      </View>
      {(languages || eta) ? (
        <View style={styles.details}>
          {languages ? <Text variant="caption">Languages: {languages}</Text> : null}
          {eta ? <Text variant="bodySm" color={colors.brand.primary}>ETA: {eta}</Text> : null}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    padding: space[4],
    borderWidth: 1,
    borderColor: semantic.border.soft,
    gap: space[3],
    ...shadows.sm,
  },
  top: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
  },
  info: {
    flex: 1,
    gap: space[1],
  },
  badges: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: space[2],
  },
  actions: {
    flexDirection: "row",
    gap: space[2],
  },
  details: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: space[3],
    paddingTop: space[2],
    borderTopWidth: 1,
    borderTopColor: semantic.border.soft,
  },
});
