import React, { PropsWithChildren } from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { Text } from "@/components/primitives/Text";
import { colors, space } from "@/theme";

type SectionProps = PropsWithChildren<{
  title: string;
  actionLabel?: string;
  onAction?: () => void;
}>;

export function Section({ title, actionLabel, onAction, children }: SectionProps) {
  return (
    <View style={styles.section}>
      <View style={styles.header}>
        <Text variant="headingSm">{title}</Text>
        {actionLabel && onAction ? (
          <Pressable onPress={onAction} accessibilityRole="button">
            <Text variant="bodySm" color={colors.brand.primary}>{actionLabel}</Text>
          </Pressable>
        ) : null}
      </View>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    gap: space[3],
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: space[4],
  },
});
