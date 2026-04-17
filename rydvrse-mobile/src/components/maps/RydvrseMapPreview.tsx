import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, semantic, shadows, space } from "@/theme";

type RydvrseMapPreviewProps = {
  pickupLabel: string;
  dropLabel: string;
  onUseCurrentLocation?: () => void;
  onOpenSearch?: () => void;
  onRecenter?: () => void;
};

export function RydvrseMapPreview({
  pickupLabel,
  dropLabel,
  onUseCurrentLocation,
  onOpenSearch,
  onRecenter,
}: RydvrseMapPreviewProps) {
  return (
    <View style={styles.mapShell} accessibilityLabel="Rydvrse map preview with pickup and drop route">
      <View style={styles.gridLayer}>
        {Array.from({ length: 8 }).map((_, index) => (
          <View key={`h-${index}`} style={[styles.gridLineHorizontal, { top: `${index * 14}%` }]} />
        ))}
        {Array.from({ length: 6 }).map((_, index) => (
          <View key={`v-${index}`} style={[styles.gridLineVertical, { left: `${index * 18}%` }]} />
        ))}
      </View>
      <View style={[styles.road, styles.roadOne]} />
      <View style={[styles.road, styles.roadTwo]} />
      <View style={[styles.road, styles.roadThree]} />
      <View style={styles.routeLine} />
      <View style={[styles.marker, styles.pickupMarker]}>
        <View style={styles.markerDot} />
      </View>
      <View style={[styles.marker, styles.dropMarker]}>
        <AppIcon name="pin" size={16} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
      </View>

      <View style={styles.topControls}>
        <Pressable style={styles.mapPill} onPress={onOpenSearch} accessibilityRole="button" accessibilityLabel="Search pickup or drop location">
          <AppIcon name="route" size={16} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          <AppText variant="caption" style={styles.mapPillText}>
            Bengaluru route preview
          </AppText>
        </Pressable>
        <Pressable style={styles.iconButton} onPress={onRecenter} accessibilityRole="button" accessibilityLabel="Recenter map">
          <AppIcon name="eta" size={18} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
        </Pressable>
      </View>

      <View style={styles.locationCard}>
        <View style={styles.locationRow}>
          <View style={[styles.locationDot, styles.pickupDot]} />
          <View style={styles.locationText}>
            <AppText variant="caption">Pickup</AppText>
            <AppText variant="bodyStrong" numberOfLines={1}>{pickupLabel}</AppText>
          </View>
        </View>
        <View style={styles.locationDivider} />
        <View style={styles.locationRow}>
          <View style={[styles.locationDot, styles.dropDot]} />
          <View style={styles.locationText}>
            <AppText variant="caption">Drop</AppText>
            <AppText variant="bodyStrong" numberOfLines={1}>{dropLabel}</AppText>
          </View>
        </View>
      </View>

      <Pressable style={styles.currentLocationButton} onPress={onUseCurrentLocation} accessibilityRole="button" accessibilityLabel="Use current location">
        <AppIcon name="pin" size={17} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
        <AppText variant="caption" style={styles.currentLocationText}>Use current location</AppText>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  mapShell: {
    flex: 1,
    minHeight: 230,
    backgroundColor: "#E9EFDF",
    overflow: "hidden",
  },
  gridLayer: {
    ...StyleSheet.absoluteFillObject,
    opacity: 0.45,
  },
  gridLineHorizontal: {
    position: "absolute",
    left: 0,
    right: 0,
    height: 1,
    backgroundColor: "rgba(16, 19, 18, 0.06)",
  },
  gridLineVertical: {
    position: "absolute",
    top: 0,
    bottom: 0,
    width: 1,
    backgroundColor: "rgba(16, 19, 18, 0.06)",
  },
  road: {
    position: "absolute",
    height: 18,
    borderRadius: radius.full,
    backgroundColor: "rgba(255,255,255,0.88)",
    borderWidth: 1,
    borderColor: "rgba(16, 19, 18, 0.06)",
  },
  roadOne: {
    width: "86%",
    top: "28%",
    left: "-8%",
    transform: [{ rotate: "-18deg" }],
  },
  roadTwo: {
    width: "96%",
    top: "56%",
    right: "-20%",
    transform: [{ rotate: "22deg" }],
  },
  roadThree: {
    width: "65%",
    top: "42%",
    left: "12%",
    transform: [{ rotate: "88deg" }],
  },
  routeLine: {
    position: "absolute",
    width: "58%",
    height: 6,
    top: "43%",
    left: "22%",
    borderRadius: radius.full,
    backgroundColor: colors.brand.strong,
    transform: [{ rotate: "-32deg" }],
  },
  marker: {
    position: "absolute",
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 3,
    borderColor: semantic.bg.surface,
    ...shadows.md,
  },
  pickupMarker: {
    left: "19%",
    top: "55%",
    backgroundColor: semantic.text.primary,
  },
  dropMarker: {
    right: "18%",
    top: "27%",
    backgroundColor: colors.brand.primary,
  },
  markerDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: colors.brand.primary,
  },
  topControls: {
    position: "absolute",
    top: space[4],
    left: space[4],
    right: space[4],
    flexDirection: "row",
    justifyContent: "space-between",
    gap: space[3],
  },
  mapPill: {
    flex: 1,
    minHeight: 42,
    borderRadius: radius.full,
    backgroundColor: "rgba(255,255,255,0.92)",
    flexDirection: "row",
    alignItems: "center",
    gap: space[2],
    paddingHorizontal: space[4],
    ...shadows.sm,
  },
  mapPillText: {
    color: semantic.text.primary,
  },
  iconButton: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: "rgba(255,255,255,0.92)",
    alignItems: "center",
    justifyContent: "center",
    ...shadows.sm,
  },
  locationCard: {
    position: "absolute",
    left: space[4],
    right: space[4],
    bottom: 58,
    backgroundColor: "rgba(255,255,255,0.94)",
    borderRadius: radius.xl,
    padding: space[3],
    gap: space[1],
    ...shadows.md,
  },
  locationRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
  },
  locationDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  pickupDot: {
    backgroundColor: semantic.text.primary,
  },
  dropDot: {
    backgroundColor: colors.brand.strong,
  },
  locationText: {
    flex: 1,
  },
  locationDivider: {
    height: 1,
    marginLeft: 24,
    backgroundColor: semantic.border.soft,
  },
  currentLocationButton: {
    position: "absolute",
    right: space[4],
    bottom: space[4],
    minHeight: 42,
    borderRadius: radius.full,
    backgroundColor: colors.brand.primary,
    flexDirection: "row",
    alignItems: "center",
    gap: space[2],
    paddingHorizontal: space[4],
    ...shadows.md,
  },
  currentLocationText: {
    color: semantic.text.onBrand,
  },
});
