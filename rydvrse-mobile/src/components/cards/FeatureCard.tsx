import React, { PropsWithChildren } from "react";
import { StyleSheet, View, ViewStyle } from "react-native";

import { colors, radius, space } from "@/theme";

type FeatureCardProps = PropsWithChildren<{
  style?: ViewStyle;
}>;

export function FeatureCard({ children, style }: FeatureCardProps) {
  return <View style={[styles.card, style]}>{children}</View>;
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.brand.soft,
    borderRadius: radius.lg,
    padding: space[5],
  },
});
