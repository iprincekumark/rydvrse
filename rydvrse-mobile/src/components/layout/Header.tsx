import React from "react";
import { StyleSheet, View, useWindowDimensions } from "react-native";

import { BrandLockup } from "@/assets/brand/BrandLockup";
import { HeaderArtwork, HeaderArtworkVariant } from "@/assets/illustrations/HeaderArtwork";
import { IconButton } from "@/components/primitives/IconButton";
import { Text } from "@/components/primitives/Text";
import { colors, semantic, space } from "@/theme";

type HeaderProps = {
  eyebrow?: string;
  title: string;
  subtitle?: string;
  visualVariant?: HeaderArtworkVariant;
  showBack?: boolean;
  onBack?: () => void;
  branded?: boolean;
};

export function Header({
  eyebrow,
  title,
  subtitle,
  visualVariant,
  showBack = false,
  onBack,
  branded = false,
}: HeaderProps) {
  const { width } = useWindowDimensions();
  const wide = width >= 760 && Boolean(visualVariant);
  const titleVariant = width >= 640 ? "displayLg" : "displaySm";

  return (
    <View style={[styles.wrapper, wide && styles.wrapperWide, branded && styles.brandedWrapper]}>
      <View style={styles.topRow}>
        {showBack && onBack ? (
          <IconButton icon="arrowRight" accessibilityLabel="Go back" onPress={onBack} style={styles.backBtn} />
        ) : null}
        <BrandLockup compact />
      </View>
      <View style={styles.copy}>
        {eyebrow ? (
          <Text variant="overline" color={branded ? colors.brand.soft : undefined}>
            {eyebrow}
          </Text>
        ) : null}
        <Text
          variant={titleVariant}
          style={[wide && styles.heroWide]}
          color={branded ? colors.neutral[0] : undefined}
          accessibilityRole="header"
        >
          {title}
        </Text>
        {subtitle ? (
          <Text
            variant="bodyMd"
            style={styles.subtitle}
            color={branded ? "rgba(255,255,255,0.8)" : undefined}
          >
            {subtitle}
          </Text>
        ) : null}
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
    gap: space[4],
    marginBottom: space[6],
    paddingTop: space[3],
  },
  wrapperWide: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  brandedWrapper: {
    backgroundColor: colors.brand.primary,
    marginHorizontal: -space[5],
    marginTop: -space[6],
    paddingHorizontal: space[5],
    paddingTop: space[8],
    paddingBottom: space[6],
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  topRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
  },
  backBtn: {
    backgroundColor: "rgba(255,255,255,0.15)",
    transform: [{ scaleX: -1 }],
  },
  copy: {
    gap: space[2],
    flex: 1,
  },
  subtitle: {
    maxWidth: 580,
    marginTop: space[1],
  },
  heroWide: {
    maxWidth: 520,
  },
  artworkCard: {
    alignSelf: "flex-start",
    padding: space[2],
    borderRadius: 18,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  artworkCardWide: {
    marginLeft: space[6],
  },
});
