import React from "react";
import { StyleSheet, View, useWindowDimensions } from "react-native";

import { BrandLockup } from "@/assets/brand/BrandLockup";
import { HeaderArtwork, HeaderArtworkVariant } from "@/assets/illustrations/HeaderArtwork";
import { AppText } from "@/components/common/AppText";
import { semantic, spacing } from "@/theme";

/**
 * Legacy HeaderBlock — updated with new spacing.
 * Prefer the new layout/Header.tsx for new code.
 */
export function HeaderBlock({
  eyebrow,
  title,
  subtitle,
  visualVariant,
}: {
  eyebrow?: string;
  title: string;
  subtitle?: string;
  visualVariant?: HeaderArtworkVariant;
}) {
  const { width } = useWindowDimensions();
  const wide = width >= 760 && Boolean(visualVariant);
  const heroScaleStyle = width >= 1024 ? styles.heroDesktop : width >= 640 ? styles.heroTablet : styles.heroMobile;

  return (
    <View style={[styles.wrapper, wide && styles.wrapperWide]}>
      <View style={styles.copy}>
        <BrandLockup compact />
        {eyebrow ? <AppText variant="overline">{eyebrow}</AppText> : null}
        <AppText variant="hero" style={[heroScaleStyle, wide && styles.heroWide]}>{title}</AppText>
        {subtitle ? <AppText variant="body" style={styles.subtitle}>{subtitle}</AppText> : null}
      </View>
      {visualVariant ? (
        <View style={[styles.artworkCard, wide && styles.artworkCardWide]}>
          <HeaderArtwork variant={visualVariant} />
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.lg,
    marginBottom: spacing.xl,
    paddingTop: spacing.sm,
  },
  wrapperWide: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  copy: {
    gap: spacing.xs,
    flex: 1,
  },
  heroMobile: {
    fontSize: 32,
    lineHeight: 38,
    letterSpacing: -0.8,
  },
  heroTablet: {
    fontSize: 42,
    lineHeight: 48,
    letterSpacing: -1.1,
  },
  heroDesktop: {
    fontSize: 50,
    lineHeight: 56,
    letterSpacing: -1.3,
  },
  subtitle: {
    maxWidth: 580,
    marginTop: spacing.xs,
  },
  heroWide: {
    maxWidth: 520,
  },
  artworkCard: {
    alignSelf: "flex-start",
    padding: spacing.xs,
    borderRadius: 18,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  artworkCardWide: {
    marginLeft: spacing.xl,
  },
});
