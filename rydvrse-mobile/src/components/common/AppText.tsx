import React, { PropsWithChildren } from "react";
import { StyleProp, Text, TextStyle } from "react-native";

import { typography } from "@/theme";

type AppTextProps = PropsWithChildren<{
  variant?: keyof typeof typography.text;
  style?: StyleProp<TextStyle>;
}>;

export function AppText({ children, variant = "body", style }: AppTextProps) {
  return <Text style={[typography.text[variant], style]}>{children}</Text>;
}
