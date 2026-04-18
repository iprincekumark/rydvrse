import React from "react";
import { StyleSheet, View } from "react-native";

import { EmptyStateArtworkVariant } from "@/assets/illustrations/EmptyStateArtwork";
import { AppText } from "@/components/common/AppText";
import { PrimaryButton } from "@/components/common/PrimaryButton";
import { radius, semantic, spacing } from "@/theme";

/**
 * Copy-first empty state. Illustrations are intentionally omitted — the
 * visual weight sat off-brand and added no information. Keep the card
 * quiet and let the copy + action do the work.
 */
export function EmptyState({
  title,
  message,
  actionLabel,
  onAction,
}: {
  title: string;
  message: string;
  actionLabel?: string;
  visualVariant?: EmptyStateArtworkVariant;
  onAction?: () => void;
}) {
  return (
    <View style={styles.card}>
      <AppText variant="section">{title}</AppText>
      <AppText variant="body" style={styles.message}>{message}</AppText>
      {actionLabel ? <PrimaryButton label={actionLabel} onPress={onAction} /> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: spacing.lg,
    gap: spacing.md,
    alignItems: "flex-start",
  },
  message: {
    maxWidth: "96%",
  },
});
