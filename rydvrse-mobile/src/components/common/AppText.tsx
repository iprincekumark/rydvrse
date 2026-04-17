import React, { PropsWithChildren } from "react";
import { StyleProp, Text, TextStyle } from "react-native";

import { typography } from "@/theme";

type AppTextProps = PropsWithChildren<{
  variant?: keyof typeof typography.text;
  style?: StyleProp<TextStyle>;
}>;

/**
 * Legacy AppText — still uses old variant keys (hero, title, section, body, bodyStrong, caption, overline).
 * Prefer the new primitives/Text.tsx for new code.
 */
export function AppText({ children, variant = "body", style }: AppTextProps) {
  return <Text style={[typography.text[variant], style]}>{children}</Text>;
}
