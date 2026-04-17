import React from "react";
import { StyleSheet, View } from "react-native";

import { BrandMark } from "@/assets/brand/BrandMark";
import { AppText } from "@/components/common/AppText";
import { semantic, spacing } from "@/theme";

type BrandLockupProps = {
  compact?: boolean;
};

export function BrandLockup({ compact = false }: BrandLockupProps) {
  return (
    <View style={[styles.row, compact && styles.compactRow]}>
      <BrandMark size={compact ? 34 : 44} />
      <View style={styles.textWrap}>
        <AppText variant={compact ? "section" : "title"} style={styles.wordmark}>
          Rydvrse
        </AppText>
        <AppText variant="caption" style={styles.tagline}>
          Trust-first private driver mobility
        </AppText>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm
  },
  compactRow: {
    gap: spacing.xs
  },
  textWrap: {
    gap: 2
  },
  wordmark: {
    color: semantic.text.primary,
    letterSpacing: 0.2
  },
  tagline: {
    color: semantic.text.secondary,
    letterSpacing: 0.5
  }
});
