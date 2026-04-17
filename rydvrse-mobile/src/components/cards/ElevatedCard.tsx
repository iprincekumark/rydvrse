import React, { PropsWithChildren } from "react";
import { StyleSheet, View, ViewStyle } from "react-native";

import { radius, semantic, shadows, space } from "@/theme";

type ElevatedCardProps = PropsWithChildren<{
  style?: ViewStyle;
}>;

export function ElevatedCard({ children, style }: ElevatedCardProps) {
  return <View style={[styles.card, style]}>{children}</View>;
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    padding: space[5],
    ...shadows.md,
  },
});
