import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppText } from "@/components/common/AppText";
import { colors, radius, semantic, shadows, spacing } from "@/theme";

/**
 * Legacy ChoiceCard — updated with new brand tokens.
 * Prefer the new cards/ServiceTypeCard.tsx for new code.
 */
export function ChoiceCard({
  title,
  subtitle,
  selected = false,
  eyebrow,
  leading,
  onPress,
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
      style={({ pressed }) => [
        styles.card,
        selected && styles.selected,
        pressed && onPress && styles.pressed,
      ]}
      onPress={onPress}
      accessibilityRole={onPress ? "button" : undefined}
      accessibilityState={{ selected }}
      accessibilityLabel={title}
    >
      <View style={styles.row}>
        {leading ? <View style={[styles.leadingWrap, selected && styles.leadingSelected]}>{leading}</View> : null}
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
    flex: 1,
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: spacing.md,
    gap: spacing.sm,
    ...shadows.sm,
  },
  selected: {
    borderColor: colors.brand.primary,
    backgroundColor: colors.brand.subtle,
    borderWidth: 2,
  },
  pressed: {
    backgroundColor: colors.neutral[50],
    transform: [{ scale: 0.98 }],
  },
  row: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: spacing.sm,
  },
  leadingWrap: {
    width: 42,
    height: 42,
    borderRadius: radius.md,
    backgroundColor: colors.neutral[100],
    alignItems: "center",
    justifyContent: "center",
  },
  leadingSelected: {
    backgroundColor: colors.brand.soft,
  },
  content: {
    flex: 1,
    gap: spacing.xs,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: spacing.md,
  },
  titleWrap: {
    flex: 1,
    gap: 2,
  },
  dot: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
    borderColor: colors.neutral[300],
  },
  dotSelected: {
    borderColor: colors.brand.primary,
    backgroundColor: colors.brand.primary,
  },
});
