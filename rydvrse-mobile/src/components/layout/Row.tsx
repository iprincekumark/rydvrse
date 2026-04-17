import React, { PropsWithChildren } from "react";
import { StyleSheet, View, ViewStyle } from "react-native";

import { space } from "@/theme";

type RowProps = PropsWithChildren<{
  gap?: keyof typeof space;
  align?: "center" | "flex-start" | "flex-end" | "stretch";
  justify?: "flex-start" | "space-between" | "center" | "flex-end";
  wrap?: boolean;
  style?: ViewStyle;
}>;

export function Row({
  children,
  gap = 3,
  align = "center",
  justify = "flex-start",
  wrap = false,
  style,
}: RowProps) {
  return (
    <View
      style={[
        styles.row,
        {
          gap: space[gap],
          alignItems: align,
          justifyContent: justify,
          flexWrap: wrap ? "wrap" : "nowrap",
        },
        style,
      ]}
    >
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
  },
});
