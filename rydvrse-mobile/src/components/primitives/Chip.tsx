import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, radius, space } from "@/theme";

type Tone = "info" | "success" | "warning" | "danger" | "neutral";

const toneMap: Record<Tone, { background: string; text: string; border: string; icon: AppIconName }> = {
  info: {
    background: colors.state.infoSoft,
    text: colors.state.info,
    border: "rgba(37,99,235,0.18)",
    icon: "status",
  },
  success: {
    background: colors.state.successSoft,
    text: colors.state.success,
    border: "rgba(5,150,105,0.18)",
    icon: "check",
  },
  warning: {
    background: colors.state.warningSoft,
    text: colors.state.warning,
    border: "rgba(217,119,6,0.18)",
    icon: "alert",
  },
  danger: {
    background: colors.state.dangerSoft,
    text: colors.state.danger,
    border: "rgba(220,38,38,0.18)",
    icon: "alert",
  },
  neutral: {
    background: colors.neutral[100],
    text: colors.neutral[700],
    border: colors.neutral[200],
    icon: "status",
  },
};

export function Chip({ label, tone = "neutral", icon }: { label: string; tone?: Tone; icon?: AppIconName }) {
  const palette = toneMap[tone];
  const resolvedIcon = icon ?? palette.icon;

  return (
    <View style={[styles.chip, { backgroundColor: palette.background, borderColor: palette.border }]}>
      <AppIcon name={resolvedIcon} size={14} color={palette.text} secondaryColor={palette.text} />
      <Text variant="caption" style={[styles.label, { color: palette.text }]}>
        {label}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  chip: {
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "flex-start",
    gap: 6,
    paddingHorizontal: space[3],
    paddingVertical: space[2],
    borderRadius: radius.full,
    borderWidth: 1,
  },
  label: {
    fontSize: 11,
    lineHeight: 14,
    letterSpacing: 0.3,
  },
});
