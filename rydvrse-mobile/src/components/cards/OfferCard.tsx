import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { Chip } from "@/components/primitives/Chip";
import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, shadows, space } from "@/theme";

type OfferCardProps = {
  serviceType: string;
  pickupZone: string;
  scheduledAt: string;
  estimatedEarning: string;
  onPress?: () => void;
};

export function OfferCard({ serviceType, pickupZone, scheduledAt, estimatedEarning, onPress }: OfferCardProps) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole={onPress ? "button" : undefined}
      accessibilityLabel={`Job offer: ${serviceType}`}
      style={({ pressed }) => [styles.card, pressed && onPress && styles.pressed]}
    >
      <View style={styles.row}>
        <View style={styles.main}>
          <View style={styles.iconWrap}>
            <AppIcon name="jobs" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
          </View>
          <View style={{ flex: 1 }}>
            <View style={styles.heading}>
              <Text variant="headingSm">{serviceType.replaceAll("_", " ")}</Text>
              <Chip label="Offer live" tone="info" />
            </View>
            <Text variant="bodySm">{pickupZone} • {scheduledAt}</Text>
          </View>
        </View>
        <View style={styles.aside}>
          <Text variant="bodyStrong">{estimatedEarning}</Text>
          <Text variant="caption" color={colors.brand.primary}>Preview earning</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    padding: space[4],
    borderWidth: 1,
    borderColor: semantic.border.soft,
    ...shadows.sm,
  },
  pressed: {
    backgroundColor: colors.neutral[50],
    transform: [{ scale: 0.98 }],
  },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: space[4],
    flexWrap: "wrap",
  },
  main: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
    flex: 1,
    minWidth: 200,
  },
  iconWrap: {
    width: 48,
    height: 48,
    borderRadius: radius.md,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  heading: {
    gap: space[2],
  },
  aside: {
    minWidth: 100,
    alignItems: "flex-end",
    gap: space[1],
  },
});
