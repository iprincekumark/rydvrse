import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, semantic, spacing } from "@/theme";

type Tone = "info" | "success" | "warning" | "danger";

const toneStyles: Record<Tone, { background: string; text: string }> = {
  info: { background: colors.state.infoSoft, text: colors.state.info },
  success: { background: colors.state.successSoft, text: colors.state.success },
  warning: { background: colors.state.warningSoft, text: colors.state.warning },
  danger: { background: colors.state.dangerSoft, text: colors.state.danger },
};

/**
 * Legacy StatusBanner — updated with new semantic colors.
 * Prefer the new feedback/Banner.tsx for new code.
 */
export function StatusBanner({ tone = "info", title, message }: { tone?: Tone; title: string; message: string }) {
  const t = toneStyles[tone];
  return (
    <View style={[styles.banner, { backgroundColor: t.background }]}>
      <View style={styles.iconWrap}>
        <AppIcon
          name={tone === "success" ? "check" : tone === "warning" || tone === "danger" ? "alert" : "shield"}
          size={18}
          color={t.text}
          secondaryColor={t.text}
        />
      </View>
      <View style={styles.content}>
        <AppText variant="bodyStrong" style={{ color: semantic.text.primary }}>
          {title}
        </AppText>
        <AppText variant="caption" style={{ color: semantic.text.secondary }}>
          {message}
        </AppText>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    borderRadius: radius.md,
    padding: spacing.md,
    gap: spacing.sm,
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
    gap: spacing.xs,
  },
});
