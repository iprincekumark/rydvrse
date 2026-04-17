import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, space } from "@/theme";

type Tone = "info" | "success" | "warning" | "danger";

const toneStyles: Record<Tone, { background: string; iconColor: string }> = {
  info: { background: colors.state.infoSoft, iconColor: colors.state.info },
  success: { background: colors.state.successSoft, iconColor: colors.state.success },
  warning: { background: colors.state.warningSoft, iconColor: colors.state.warning },
  danger: { background: colors.state.dangerSoft, iconColor: colors.state.danger },
};

type BannerProps = {
  tone?: Tone;
  title: string;
  message: string;
  dismissible?: boolean;
  onDismiss?: () => void;
};

export function Banner({ tone = "info", title, message, dismissible = false, onDismiss }: BannerProps) {
  const palette = toneStyles[tone];

  return (
    <View style={[styles.banner, { backgroundColor: palette.background }]}>
      <View style={styles.iconWrap}>
        <AppIcon
          name={tone === "success" ? "check" : tone === "warning" || tone === "danger" ? "alert" : "shield"}
          size={18}
          color={palette.iconColor}
          secondaryColor={palette.iconColor}
        />
      </View>
      <View style={styles.content}>
        <Text variant="bodyStrong">{title}</Text>
        <Text variant="bodySm" color={semantic.text.secondary}>{message}</Text>
      </View>
      {dismissible && onDismiss ? (
        <Pressable onPress={onDismiss} accessibilityLabel="Dismiss" style={styles.dismiss}>
          <AppIcon name="alert" size={16} color={semantic.text.muted} secondaryColor={semantic.text.muted} />
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    borderRadius: radius.md,
    padding: space[3],
    gap: space[3],
    flexDirection: "row",
    alignItems: "flex-start",
  },
  iconWrap: {
    width: 32,
    height: 32,
    borderRadius: radius.sm,
    backgroundColor: "rgba(255,255,255,0.7)",
    alignItems: "center",
    justifyContent: "center",
  },
  content: {
    flex: 1,
    gap: space[1],
  },
  dismiss: {
    padding: space[1],
  },
});
