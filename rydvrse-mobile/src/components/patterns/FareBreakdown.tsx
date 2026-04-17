import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, space } from "@/theme";

type FareItem = {
  label: string;
  value: string;
  icon?: AppIconName;
};

type FareBreakdownProps = {
  items: FareItem[];
  total?: { label: string; value: string };
};

function resolveIcon(label: string): AppIconName {
  const n = label.toLowerCase();
  if (n.includes("fare") || n.includes("total") || n.includes("earning") || n.includes("payout")) return "wallet";
  if (n.includes("service")) return "car";
  if (n.includes("night") || n.includes("bonus")) return "star";
  if (n.includes("arrival")) return "pin";
  return "document";
}

export function FareBreakdown({ items, total }: FareBreakdownProps) {
  return (
    <View style={styles.card}>
      {items.map((item) => (
        <View key={item.label} style={styles.row}>
          <View style={styles.labelWrap}>
            <View style={styles.iconWrap}>
              <AppIcon
                name={item.icon ?? resolveIcon(item.label)}
                size={15}
                color={colors.brand.primary}
                secondaryColor={colors.neutral[400]}
              />
            </View>
            <Text variant="bodySm" color={semantic.text.secondary}>{item.label}</Text>
          </View>
          <Text variant="bodyStrong">{item.value}</Text>
        </View>
      ))}
      {total ? (
        <>
          <View style={styles.divider} />
          <View style={styles.row}>
            <Text variant="headingSm">{total.label}</Text>
            <Text variant="headingSm" color={colors.brand.primary}>{total.value}</Text>
          </View>
        </>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    padding: space[4],
    borderWidth: 1,
    borderColor: semantic.border.soft,
    gap: space[2],
  },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: space[4],
    paddingVertical: space[1],
  },
  labelWrap: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
    flex: 1,
  },
  iconWrap: {
    width: 28,
    height: 28,
    borderRadius: radius.sm,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  divider: {
    height: 1,
    backgroundColor: semantic.border.soft,
    marginVertical: space[1],
  },
});
