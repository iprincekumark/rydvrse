import React from "react";
import { ActivityIndicator, Pressable, StyleSheet, View, ViewStyle } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, fontFamily, radius, semantic, space } from "@/theme";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";

type ButtonProps = {
  label: string;
  variant?: ButtonVariant;
  onPress?: () => void;
  disabled?: boolean;
  loading?: boolean;
  leadingIcon?: AppIconName;
  trailingIcon?: AppIconName;
  style?: ViewStyle;
};

const variantStyles: Record<ButtonVariant, { bg: string; border: string; text: string; pressedBg: string }> = {
  primary: {
    bg: colors.brand.primary,
    border: colors.brand.primary,
    text: colors.neutral[0],
    pressedBg: colors.brand.strong,
  },
  secondary: {
    bg: colors.neutral[0],
    border: colors.neutral[300],
    text: colors.neutral[900],
    pressedBg: colors.brand.soft,
  },
  ghost: {
    bg: "transparent",
    border: "transparent",
    text: colors.brand.primary,
    pressedBg: colors.brand.subtle,
  },
  danger: {
    bg: colors.state.danger,
    border: colors.state.danger,
    text: colors.neutral[0],
    pressedBg: "#B91C1C",
  },
};

export function Button({
  label,
  variant = "primary",
  onPress,
  disabled = false,
  loading = false,
  leadingIcon,
  trailingIcon,
  style,
}: ButtonProps) {
  const vs = variantStyles[variant];
  const iconColor = vs.text;
  const isDisabled = disabled || loading;

  return (
    <Pressable
      disabled={isDisabled}
      onPress={onPress}
      accessibilityRole="button"
      accessibilityState={{ disabled: isDisabled }}
      accessibilityLabel={label}
      style={({ pressed }) => [
        styles.base,
        {
          backgroundColor: pressed && !isDisabled ? vs.pressedBg : vs.bg,
          borderColor: vs.border,
        },
        variant === "ghost" && styles.ghost,
        isDisabled && styles.disabled,
        pressed && !isDisabled && styles.pressed,
        style,
      ]}
    >
      <View style={styles.inner}>
        {loading ? (
          <ActivityIndicator size="small" color={iconColor} />
        ) : leadingIcon ? (
          <AppIcon name={leadingIcon} size={18} color={iconColor} secondaryColor={iconColor} />
        ) : null}
        <Text
          variant="bodyStrong"
          style={[styles.label, { color: vs.text }]}
        >
          {label}
        </Text>
        {!loading && trailingIcon ? (
          <AppIcon name={trailingIcon} size={18} color={iconColor} secondaryColor={iconColor} />
        ) : null}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 52,
    borderRadius: radius.md,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: space[5],
    borderWidth: 1,
  },
  ghost: {
    minHeight: 44,
    paddingHorizontal: space[3],
    borderWidth: 0,
  },
  inner: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: space[2],
  },
  disabled: {
    opacity: 0.45,
  },
  pressed: {
    transform: [{ scale: 0.96 }],
    opacity: 0.85,
  },
  label: {
    fontFamily: fontFamily.bold,
  },
});
