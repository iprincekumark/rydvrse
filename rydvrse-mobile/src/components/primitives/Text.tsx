import React, { PropsWithChildren } from "react";
import { StyleProp, Text as RNText, TextStyle } from "react-native";

import { textStyle } from "@/theme";

export type TextVariant = keyof typeof textStyle;

type TextProps = PropsWithChildren<{
  variant?: TextVariant;
  color?: string;
  style?: StyleProp<TextStyle>;
  numberOfLines?: number;
  accessibilityRole?: "header" | "text" | "link" | "none";
}>;

export function Text({
  children,
  variant = "bodyMd",
  color,
  style,
  numberOfLines,
  accessibilityRole,
}: TextProps) {
  return (
    <RNText
      style={[textStyle[variant], color ? { color } : null, style]}
      numberOfLines={numberOfLines}
      accessibilityRole={accessibilityRole}
    >
      {children}
    </RNText>
  );
}
