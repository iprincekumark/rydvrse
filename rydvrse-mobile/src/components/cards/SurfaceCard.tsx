import React, { PropsWithChildren } from "react";
import { StyleSheet, View, ViewStyle } from "react-native";

import { radius, semantic, shadows, space } from "@/theme";

type SurfaceCardProps = PropsWithChildren<{
  style?: ViewStyle;
}>;

export function SurfaceCard({ children, style }: SurfaceCardProps) {
  return <View style={[styles.card, style]}>{children}</View>;
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    padding: space[4],
    borderWidth: 1,
    borderColor: semantic.border.soft,
    ...shadows.sm,
  },
});
