import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { ServiceIcon } from "@/assets/icons/ServiceIcon";
import { Chip } from "@/components/primitives/Chip";
import { Text } from "@/components/primitives/Text";
import { CustomerServiceType } from "@/constants/serviceTypes";
import { colors, radius, semantic, shadows, space } from "@/theme";

type BookingCardProps = {
  title: string;
  subtitle: string;
  amount: string;
  serviceType: string;
  status: string;
  onPress?: () => void;
};

function resolveStatusTone(status: string): "info" | "success" | "warning" | "neutral" {
  const normalized = status.toLowerCase();
  if (normalized.includes("completed") || normalized.includes("confirmed") || normalized.includes("assigned")) return "success";
  if (normalized.includes("pending") || normalized.includes("requested") || normalized.includes("created")) return "info";
  if (normalized.includes("cancel") || normalized.includes("delay") || normalized.includes("issue")) return "warning";
  return "neutral";
}

export function BookingCard({ title, subtitle, amount, serviceType, status, onPress }: BookingCardProps) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole={onPress ? "button" : undefined}
      accessibilityLabel={title}
      style={({ pressed }) => [styles.card, pressed && onPress && styles.pressed]}
    >
      <View style={styles.row}>
        <View style={styles.main}>
          <View style={styles.iconWrap}>
            <ServiceIcon serviceType={serviceType as CustomerServiceType} />
          </View>
          <View style={styles.content}>
            <View style={styles.header}>
              <Text variant="headingSm" numberOfLines={1}>{title}</Text>
              <Chip label={status.replaceAll("_", " ")} tone={resolveStatusTone(status)} />
            </View>
            <Text variant="bodySm" numberOfLines={1}>{subtitle}</Text>
          </View>
        </View>
        <View style={styles.aside}>
          <Text variant="bodyStrong">{amount}</Text>
          <Text variant="caption" color={colors.brand.primary}>Fare</Text>
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
  content: {
    flex: 1,
    gap: space[1],
  },
  header: {
    gap: space[2],
  },
  aside: {
    minWidth: 90,
    alignItems: "flex-end",
    gap: space[1],
  },
});
