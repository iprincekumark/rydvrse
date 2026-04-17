import React, { PropsWithChildren } from "react";
import { StyleProp, Text, TextProps, TextStyle } from "react-native";

import { typography } from "@/theme";

type AppTextProps = PropsWithChildren<TextProps & {
  variant?: keyof typeof typography.text;
  style?: StyleProp<TextStyle>;
}>;

/**
 * Legacy AppText — still uses old variant keys (hero, title, section, body, bodyStrong, caption, overline).
 * Prefer the new primitives/Text.tsx for new code.
 */
export function AppText({ children, variant = "body", style, ...props }: AppTextProps) {
  return <Text {...props} style={[typography.text[variant], style]}>{children}</Text>;
}
