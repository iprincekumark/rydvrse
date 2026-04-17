import React from "react";
import { Pressable, StyleSheet, ViewStyle } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { colors, semantic, space } from "@/theme";

type IconButtonProps = {
  icon: AppIconName;
  size?: number;
  onPress?: () => void;
  accessibilityLabel: string;
  style?: ViewStyle;
};

export function IconButton({
  icon,
  size = 44,
  onPress,
  accessibilityLabel,
  style,
}: IconButtonProps) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      style={({ pressed }) => [
        styles.base,
        { width: size, height: size, borderRadius: size / 2 },
        pressed && styles.pressed,
        style,
      ]}
    >
      <AppIcon
        name={icon}
        size={size * 0.45}
        color={semantic.text.primary}
        secondaryColor={semantic.text.muted}
      />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    backgroundColor: colors.neutral[100],
    alignItems: "center",
    justifyContent: "center",
  },
  pressed: {
    backgroundColor: colors.neutral[200],
    transform: [{ scale: 0.92 }],
  },
});
