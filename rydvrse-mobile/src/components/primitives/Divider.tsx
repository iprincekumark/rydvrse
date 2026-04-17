import React from "react";
import { StyleSheet, View } from "react-native";

import { Text } from "@/components/primitives/Text";
import { colors, space } from "@/theme";

type DividerProps = {
  label?: string;
};

export function Divider({ label }: DividerProps) {
  if (label) {
    return (
      <View style={styles.labeled}>
        <View style={styles.line} />
        <Text variant="caption" style={styles.label}>{label}</Text>
        <View style={styles.line} />
      </View>
    );
  }

  return <View style={styles.line} />;
}

const styles = StyleSheet.create({
  line: {
    flex: 1,
    height: 1,
    backgroundColor: colors.neutral[200],
  },
  labeled: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
  },
  label: {
    color: colors.neutral[400],
  },
});
