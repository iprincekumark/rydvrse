import React from "react";
import { StyleSheet, View } from "react-native";

import { BrandLockup } from "@/assets/brand/BrandLockup";
import { HeaderArtwork, HeaderArtworkVariant } from "@/assets/illustrations/HeaderArtwork";
import { AppText } from "@/components/common/AppText";
import { colors, spacing } from "@/theme";

export function HeaderBlock({
  eyebrow,
  title,
  subtitle,
  visualVariant
}: {
  eyebrow?: string;
  title: string;
  subtitle?: string;
  visualVariant?: HeaderArtworkVariant;
}) {
  return (
    <View style={styles.wrapper}>
      <BrandLockup compact />
      {eyebrow ? <AppText variant="overline">{eyebrow}</AppText> : null}
      <AppText variant="hero">{title}</AppText>
      {subtitle ? <AppText variant="body" style={styles.subtitle}>{subtitle}</AppText> : null}
      {visualVariant ? (
        <View style={styles.artworkCard}>
          <HeaderArtwork variant={visualVariant} />
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.xs,
    marginBottom: spacing.sm
  },
  subtitle: {
    maxWidth: "94%"
  },
  artworkCard: {
    marginTop: spacing.sm,
    alignSelf: "flex-start",
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: 28,
    backgroundColor: colors.background.surface,
    borderWidth: 1,
    borderColor: colors.border.soft
  }
});
