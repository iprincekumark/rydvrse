import React, { PropsWithChildren } from "react";
import { StyleSheet, View } from "react-native";

import { colors, radius, shadows, spacing } from "@/theme";

export function SectionCard({ children }: PropsWithChildren) {
  return <View style={styles.card}>{children}</View>;
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.background.surface,
    borderRadius: radius.lg,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: colors.border.soft,
    ...shadows.card
  }
});
