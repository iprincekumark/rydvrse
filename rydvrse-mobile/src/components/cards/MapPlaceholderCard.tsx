import React from "react";
import { StyleSheet, View } from "react-native";

import { RouteArtwork } from "@/assets/illustrations/RouteArtwork";
import { AppText } from "@/components/common/AppText";
import { colors, radius, spacing } from "@/theme";

export function MapPlaceholderCard({ title = "Live map preview", subtitle = "Tracking and route context appear here during the trip." }: { title?: string; subtitle?: string }) {
  return (
    <View style={styles.card}>
      <View style={styles.artWrap}>
        <RouteArtwork />
      </View>
      <AppText variant="section">{title}</AppText>
      <AppText variant="body">{subtitle}</AppText>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    minHeight: 180,
    borderRadius: radius.xl,
    backgroundColor: colors.background.surface,
    borderWidth: 1,
    borderColor: colors.border.soft,
    padding: spacing.lg,
    justifyContent: "flex-end",
    overflow: "hidden"
  },
  artWrap: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0
  }
});
