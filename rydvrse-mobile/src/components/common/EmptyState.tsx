import React from "react";
import { StyleSheet, View } from "react-native";

import { EmptyStateArtwork, EmptyStateArtworkVariant } from "@/assets/illustrations/EmptyStateArtwork";
import { AppText } from "@/components/common/AppText";
import { PrimaryButton } from "@/components/common/PrimaryButton";
import { radius, semantic, spacing } from "@/theme";

export function EmptyState({
  title,
  message,
  actionLabel,
  visualVariant = "booking",
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
      <View style={styles.visualWrap}>
        <EmptyStateArtwork variant={visualVariant} />
      </View>
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
  visualWrap: {
    alignSelf: "stretch",
    alignItems: "center",
    paddingBottom: spacing.xs,
  },
  message: {
    maxWidth: "96%",
  },
});
