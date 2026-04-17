import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, spacing } from "@/theme";

type Tone = "info" | "success" | "warning" | "danger" | "neutral";

const toneMap: Record<Tone, { background: string; text: string; border: string; icon: AppIconName }> = {
  info: {
    background: colors.state.infoSoft,
    text: colors.state.info,
    border: "rgba(0,153,255,0.18)",
    icon: "status"
  },
  success: {
    background: colors.state.successSoft,
    text: colors.state.success,
    border: "rgba(22,116,71,0.18)",
    icon: "check"
  },
  warning: {
    background: colors.state.warningSoft,
    text: colors.state.warning,
    border: "rgba(140,90,18,0.18)",
    icon: "alert"
  },
  danger: {
    background: colors.state.dangerSoft,
    text: colors.state.danger,
    border: "rgba(196,60,60,0.18)",
    icon: "alert"
  },
  neutral: {
    background: colors.background.muted,
    text: colors.text.secondary,
    border: colors.border.soft,
    icon: "status"
  }
};

export function StatusChip({ label, tone = "neutral" }: { label: string; tone?: Tone }) {
  const palette = toneMap[tone];

  return (
    <View style={[styles.chip, { backgroundColor: palette.background, borderColor: palette.border }]}>
      <AppIcon name={palette.icon} size={14} color={palette.text} secondaryColor={palette.text} />
      <AppText variant="caption" style={[styles.label, { color: palette.text }]}>
        {label}
      </AppText>
    </View>
  );
}

const styles = StyleSheet.create({
  chip: {
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "flex-start",
    gap: 6,
    paddingHorizontal: spacing.sm,
    paddingVertical: 6,
    borderRadius: radius.pill,
    borderWidth: 1
  },
  label: {
    fontSize: 11,
    lineHeight: 14,
    letterSpacing: 0.3
  }
});
