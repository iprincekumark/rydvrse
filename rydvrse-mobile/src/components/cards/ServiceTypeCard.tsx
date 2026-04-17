import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, shadows, space } from "@/theme";

type ServiceTypeCardProps = {
  title: string;
  subtitle: string;
  eyebrow?: string;
  selected?: boolean;
  leading?: React.ReactNode;
  onPress?: () => void;
};

export function ServiceTypeCard({
  title,
  subtitle,
  selected = false,
  eyebrow,
  leading,
  onPress,
}: ServiceTypeCardProps) {
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
              {eyebrow ? <Text variant="overline">{eyebrow}</Text> : null}
              <Text variant="headingSm">{title}</Text>
            </View>
            <View style={[styles.dot, selected && styles.dotSelected]} />
          </View>
          <Text variant="bodySm">{subtitle}</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: space[5],
    gap: space[3],
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
    gap: space[4],
  },
  leadingWrap: {
    width: 52,
    height: 52,
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
    gap: space[2],
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: space[4],
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
