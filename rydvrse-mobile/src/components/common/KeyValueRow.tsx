import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, spacing } from "@/theme";

function resolveIcon(label: string): AppIconName {
  const normalized = label.toLowerCase();
  if (normalized.includes("pickup") || normalized.includes("drop")) return "pin";
  if (normalized.includes("schedule") || normalized.includes("duration") || normalized.includes("timer")) return "clock";
  if (normalized.includes("service")) return "car";
  if (normalized.includes("fare") || normalized.includes("earning") || normalized.includes("payout") || normalized.includes("payment") || normalized.includes("total")) return "wallet";
  if (normalized.includes("driver")) return "driver";
  if (normalized.includes("rating")) return "star";
  if (normalized.includes("language")) return "language";
  if (normalized.includes("eta")) return "eta";
  if (normalized.includes("verification") || normalized.includes("compliance")) return "shield";
  if (normalized.includes("booking id") || normalized.includes("assignment")) return "ticket";
  if (normalized.includes("status") || normalized.includes("onboarding") || normalized.includes("invoice")) return "status";
  if (normalized.includes("city")) return "city";
  if (normalized.includes("mobile")) return "phone";
  if (normalized.includes("name")) return "profile";
  return "document";
}

export function KeyValueRow({ label, value, icon }: { label: string; value: string; icon?: AppIconName }) {
  const resolvedIcon = icon ?? resolveIcon(label);

  return (
    <View style={styles.row}>
      <View style={styles.labelWrap}>
        <View style={styles.iconWrap}>
          <AppIcon name={resolvedIcon} size={15} color={colors.primary.base} secondaryColor={colors.secondary.amber} />
        </View>
        <AppText variant="caption" style={styles.label}>{label}</AppText>
      </View>
      <View style={styles.valueWrap}>
        <AppText variant="bodyStrong" style={styles.value}>
          {value}
        </AppText>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: spacing.md,
    paddingVertical: spacing.xs
  },
  labelWrap: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    flex: 1
  },
  iconWrap: {
    width: 28,
    height: 28,
    borderRadius: radius.md,
    backgroundColor: colors.primary.soft,
    alignItems: "center",
    justifyContent: "center"
  },
  label: {
    color: colors.text.secondary
  },
  valueWrap: {
    flexShrink: 1,
    maxWidth: "52%"
  },
  value: {
    color: colors.text.primary,
    textAlign: "right",
    flexShrink: 1
  }
});
