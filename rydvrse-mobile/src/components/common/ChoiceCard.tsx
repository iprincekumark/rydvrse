import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppText } from "@/components/common/AppText";
import { colors, radius, shadows, spacing } from "@/theme";

export function ChoiceCard({
  title,
  subtitle,
  selected = false,
  eyebrow,
  leading,
  onPress
}: {
  title: string;
  subtitle: string;
  selected?: boolean;
  eyebrow?: string;
  leading?: React.ReactNode;
  onPress?: () => void;
}) {
  return (
    <Pressable
      style={[styles.card, selected && styles.selected]}
      onPress={onPress}
      accessibilityRole={onPress ? "button" : undefined}
      accessibilityState={{ selected }}
      accessibilityLabel={title}
    >
      <View style={styles.row}>
        {leading ? <View style={styles.leadingWrap}>{leading}</View> : null}
        <View style={styles.content}>
          <View style={styles.header}>
            <View style={styles.titleWrap}>
              {eyebrow ? <AppText variant="overline">{eyebrow}</AppText> : null}
              <AppText variant="section">{title}</AppText>
            </View>
            <View style={[styles.dot, selected && styles.dotSelected]} />
          </View>
          <AppText variant="body">{subtitle}</AppText>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.lg,
    backgroundColor: colors.background.surface,
    borderWidth: 1,
    borderColor: colors.border.soft,
    padding: spacing.lg,
    gap: spacing.sm,
    ...shadows.card
  },
  row: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: spacing.md
  },
  leadingWrap: {
    width: 56,
    height: 56,
    borderRadius: radius.lg,
    backgroundColor: colors.background.muted,
    alignItems: "center",
    justifyContent: "center"
  },
  content: {
    flex: 1,
    gap: spacing.xs
  },
  selected: {
    borderColor: colors.primary.base,
    backgroundColor: colors.background.muted
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: spacing.md
  },
  titleWrap: {
    flex: 1,
    gap: 2
  },
  dot: {
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 2,
    borderColor: colors.border.strong
  },
  dotSelected: {
    borderColor: colors.primary.base,
    backgroundColor: colors.primary.base
  }
});
