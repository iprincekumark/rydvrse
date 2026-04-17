import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { PrimaryButton } from "@/components/common/PrimaryButton";
import { CustomerServiceType } from "@/constants/serviceTypes";
import { colors, radius, semantic, shadows, space } from "@/theme";

type BookingHomeSheetProps = {
  serviceType: CustomerServiceType;
  pickup: string;
  drop: string;
  scheduleLabel: string;
  onTripTypeChange: (serviceType: CustomerServiceType) => void;
  onQuickSchedule: (kind: "now" | "thirty" | "hour") => void;
  onOpenLocationSearch: () => void;
  onOpenDetails: () => void;
  onGetFare: () => void;
};

export function BookingHomeSheet({
  serviceType,
  pickup,
  drop,
  scheduleLabel,
  onTripTypeChange,
  onQuickSchedule,
  onOpenLocationSearch,
  onOpenDetails,
  onGetFare,
}: BookingHomeSheetProps) {
  const selectedOneWay = serviceType !== "ROUND_TRIP";

  return (
    <View style={styles.sheet}>
      <View style={styles.handle} />
      <View style={styles.headerRow}>
        <View>
          <AppText variant="overline" style={styles.eyebrow}>Rydvrse</AppText>
          <AppText variant="title" style={styles.title}>Book Driver</AppText>
        </View>
        <View style={styles.cityBadge}>
          <AppIcon name="city" size={15} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
          <AppText variant="caption" style={styles.cityBadgeText}>BLR</AppText>
        </View>
      </View>

      <View style={styles.tripSegment} accessibilityRole="tablist">
        <Pressable
          style={[styles.segmentButton, selectedOneWay && styles.segmentSelected]}
          onPress={() => onTripTypeChange("ONE_WAY_DROP")}
          accessibilityRole="tab"
          accessibilityState={{ selected: selectedOneWay }}
          accessibilityLabel="Select one-way trip"
        >
          <AppIcon name="route" size={16} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          <AppText variant="bodyStrong">One-way</AppText>
        </Pressable>
        <Pressable
          style={[styles.segmentButton, serviceType === "ROUND_TRIP" && styles.segmentSelected]}
          onPress={() => onTripTypeChange("ROUND_TRIP")}
          accessibilityRole="tab"
          accessibilityState={{ selected: serviceType === "ROUND_TRIP" }}
          accessibilityLabel="Select round trip"
        >
          <AppIcon name="clock" size={16} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          <AppText variant="bodyStrong">Round trip</AppText>
        </Pressable>
      </View>

      <Pressable style={styles.locationBox} onPress={onOpenLocationSearch} accessibilityRole="button" accessibilityLabel="Edit pickup and drop locations">
        <View style={styles.locationLine}>
          <View style={[styles.dot, styles.pickupDot]} />
          <View style={styles.locationCopy}>
            <AppText variant="caption">Pickup</AppText>
            <AppText variant="bodyStrong" numberOfLines={1}>{pickup}</AppText>
          </View>
        </View>
        <View style={styles.verticalLine} />
        <View style={styles.locationLine}>
          <View style={[styles.dot, styles.dropDot]} />
          <View style={styles.locationCopy}>
            <AppText variant="caption">Drop</AppText>
            <AppText variant="bodyStrong" numberOfLines={1}>{drop}</AppText>
          </View>
        </View>
      </Pressable>

      <View style={styles.quickScheduleRow}>
        {[
          { key: "now", label: "Now" },
          { key: "thirty", label: "30 min" },
          { key: "hour", label: "1 hour" },
        ].map((item) => (
          <Pressable
            key={item.key}
            style={styles.scheduleChip}
            onPress={() => onQuickSchedule(item.key as "now" | "thirty" | "hour")}
            accessibilityRole="button"
            accessibilityLabel={`Schedule ${item.label}`}
          >
            <AppText variant="caption" style={styles.scheduleChipText}>{item.label}</AppText>
          </Pressable>
        ))}
      </View>

      <View style={styles.metricsRow}>
        <View style={styles.metric}>
          <AppIcon name="calendar" size={15} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          <View style={styles.metricCopy}>
            <AppText variant="caption">Pickup time</AppText>
            <AppText variant="bodyStrong">{scheduleLabel}</AppText>
          </View>
        </View>
      </View>

      <View style={styles.actionRow}>
        <Pressable style={styles.detailsButton} onPress={onOpenDetails} accessibilityRole="button" accessibilityLabel="Edit full booking details">
          <AppText variant="bodyStrong">Details</AppText>
        </Pressable>
        <PrimaryButton label="Get fare" onPress={onGetFare} trailingIcon="arrowRight" style={styles.fareButton} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  sheet: {
    backgroundColor: semantic.bg.surface,
    borderTopLeftRadius: 30,
    borderTopRightRadius: 30,
    paddingHorizontal: space[3],
    paddingTop: space[2],
    paddingBottom: space[3],
    gap: space[2],
    borderWidth: 1,
    borderColor: "rgba(16,19,18,0.08)",
    ...shadows.lg,
  },
  handle: {
    width: 44,
    height: 5,
    borderRadius: radius.full,
    backgroundColor: semantic.border.strong,
    alignSelf: "center",
    marginBottom: 0,
  },
  headerRow: {
    flexDirection: "row",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: space[3],
  },
  eyebrow: {
    color: colors.brand.strong,
  },
  title: {
    color: semantic.text.primary,
    fontSize: 21,
    lineHeight: 26,
  },
  cityBadge: {
    minHeight: 36,
    borderRadius: radius.full,
    backgroundColor: semantic.text.primary,
    flexDirection: "row",
    alignItems: "center",
    gap: space[1],
    paddingHorizontal: space[3],
  },
  cityBadgeText: {
    color: semantic.text.inverted,
  },
  tripSegment: {
    flexDirection: "row",
    backgroundColor: semantic.bg.muted,
    borderRadius: radius.full,
    padding: space[1],
    gap: space[1],
  },
  segmentButton: {
    flex: 1,
    minHeight: 34,
    borderRadius: radius.full,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: space[2],
  },
  segmentSelected: {
    backgroundColor: colors.brand.primary,
    ...shadows.sm,
  },
  locationBox: {
    borderRadius: radius.xl,
    backgroundColor: semantic.bg.elevated,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: space[2],
    gap: space[1],
  },
  locationLine: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
  },
  dot: {
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
  locationCopy: {
    flex: 1,
  },
  verticalLine: {
    width: 1,
    height: 14,
    backgroundColor: semantic.border.strong,
    marginLeft: 5.5,
  },
  quickScheduleRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: space[2],
  },
  scheduleChip: {
    minHeight: 28,
    borderRadius: radius.full,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: space[3],
  },
  scheduleChipText: {
    color: semantic.text.primary,
  },
  metricsRow: {
    flexDirection: "row",
  },
  metric: {
    flex: 1,
    minHeight: 46,
    borderRadius: radius.md,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    flexDirection: "row",
    alignItems: "center",
    gap: space[2],
    padding: space[2],
  },
  metricCopy: {
    flex: 1,
  },
  actionRow: {
    flexDirection: "row",
    gap: space[2],
  },
  detailsButton: {
    minHeight: 48,
    borderRadius: radius.md,
    paddingHorizontal: space[4],
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  fareButton: {
    flex: 1,
  },
});
