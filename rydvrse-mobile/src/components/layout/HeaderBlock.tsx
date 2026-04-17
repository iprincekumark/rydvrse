import React from "react";
import { StyleSheet, View, useWindowDimensions } from "react-native";

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
    paddingTop: spacing.sm
  },
  wrapperWide: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between"
  },
  copy: {
    gap: spacing.xs,
    flex: 1
  },
  heroMobile: {
    fontSize: 36,
    lineHeight: 40,
    letterSpacing: -0.8
  },
  heroTablet: {
    fontSize: 46,
    lineHeight: 50,
    letterSpacing: -1.15
  },
  heroDesktop: {
    fontSize: 56,
    lineHeight: 62,
    letterSpacing: -1.4
  },
  subtitle: {
    maxWidth: 620,
    marginTop: spacing.xs
  },
  heroWide: {
    maxWidth: 560
  },
  artworkCard: {
    alignSelf: "flex-start",
    padding: spacing.xs,
    borderRadius: 18,
    backgroundColor: colors.background.surface,
    borderWidth: 1,
    borderColor: colors.border.soft
  },
  artworkCardWide: {
    marginLeft: spacing.xl
  }
});
