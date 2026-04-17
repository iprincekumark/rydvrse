import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, spacing } from "@/theme";

type Tone = "info" | "success" | "warning" | "danger";

const toneStyles: Record<Tone, { background: string; text: string }> = {
  info: { background: colors.state.infoSoft, text: colors.state.info },
  success: { background: colors.state.successSoft, text: colors.state.success },
  warning: { background: colors.state.warningSoft, text: colors.state.warning },
  danger: { background: colors.state.dangerSoft, text: colors.state.danger }
};

export function StatusBanner({ tone = "info", title, message }: { tone?: Tone; title: string; message: string }) {
  const theme = toneStyles[tone];
  return (
    <View style={[styles.banner, { backgroundColor: theme.background, borderColor: colors.border.soft }]}>
      <View style={styles.iconWrap}>
        <AppIcon
          name={tone === "success" ? "check" : tone === "warning" || tone === "danger" ? "alert" : "shield"}
          size={18}
          color={theme.text}
          secondaryColor={theme.text}
        />
      </View>
      <View style={styles.content}>
        <AppText variant="bodyStrong" style={{ color: colors.text.primary }}>
          {title}
        </AppText>
        <AppText variant="caption" style={{ color: colors.text.secondary }}>
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
    borderWidth: 1,
    flexDirection: "row",
    alignItems: "flex-start"
  },
  iconWrap: {
    width: 30,
    height: 30,
    borderRadius: radius.md,
    backgroundColor: colors.background.surface,
    alignItems: "center",
    justifyContent: "center"
  },
  content: {
    flex: 1,
    gap: spacing.xs
  }
});
