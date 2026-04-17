import React, { PropsWithChildren } from "react";
import { StyleSheet, View } from "react-native";

import { radius, semantic, shadows, space } from "@/theme";

/**
 * Legacy SectionCard — now delegates to the same layout with new tokens.
 * Prefer SurfaceCard for new code.
 */
export function SectionCard({ children }: PropsWithChildren) {
  return <View style={styles.card}>{children}</View>;
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.md,
    padding: space[4],
    borderWidth: 1,
    borderColor: semantic.border.soft,
    ...shadows.sm,
  },
});
