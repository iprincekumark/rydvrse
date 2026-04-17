import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, radius, semantic, space } from "@/theme";

type SignalItem = {
  icon: AppIconName;
  title: string;
  subtitle: string;
};

type SignalStripProps = {
  items: SignalItem[];
};

export function SignalStrip({ items }: SignalStripProps) {
  return (
    <View style={styles.container}>
      {items.map((item) => (
        <View key={item.title} style={styles.card}>
          <View style={styles.iconWrap}>
            <AppIcon name={item.icon} size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
          </View>
          <View style={styles.text}>
            <Text variant="bodyStrong">{item.title}</Text>
            <Text variant="caption">{item.subtitle}</Text>
          </View>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: semantic.bg.surface,
    borderRadius: radius.lg,
    padding: space[4],
    borderWidth: 1,
    borderColor: semantic.border.soft,
    gap: space[3],
  },
  card: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
    paddingVertical: space[1],
  },
  iconWrap: {
    width: 40,
    height: 40,
    borderRadius: radius.md,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  text: {
    flex: 1,
    gap: 2,
  },
});
