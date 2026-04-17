import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, space } from "@/theme";

type MetricItem = {
  icon: AppIconName;
  label: string;
  value: string;
};

type MetricRowProps = {
  items: MetricItem[];
};

export function MetricRow({ items }: MetricRowProps) {
  return (
    <View style={styles.strip}>
      {items.map((item) => (
        <View key={item.label} style={styles.card}>
          <View style={styles.iconWrap}>
            <AppIcon name={item.icon} size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
          </View>
          <Text variant="caption">{item.label}</Text>
          <Text variant="bodyStrong">{item.value}</Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  strip: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: space[3],
  },
  card: {
    flex: 1,
    minWidth: 130,
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: space[4],
    gap: space[2],
  },
  iconWrap: {
    width: 36,
    height: 36,
    borderRadius: radius.md,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
});
