import React from "react";
import { Pressable, StyleSheet, View, ViewStyle } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, semantic, spacing, typography } from "@/theme";

type PrimaryButtonProps = {
  label: string;
  onPress?: () => void;
  secondary?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
  leadingIcon?: AppIconName;
  trailingIcon?: AppIconName;
};

/**
 * Legacy PrimaryButton — uses new brand colors.
 * Prefer the new primitives/Button.tsx for new code.
 */
export function PrimaryButton({
  label,
  onPress,
  secondary = false,
  disabled = false,
  style,
  leadingIcon,
  trailingIcon,
}: PrimaryButtonProps) {
  const iconColor = secondary ? semantic.text.primary : semantic.text.inverted;

  return (
    <Pressable
      disabled={disabled}
      onPress={onPress}
      accessibilityRole="button"
      accessibilityState={{ disabled }}
      accessibilityLabel={label}
      style={({ pressed }) => [
        styles.base,
        secondary ? styles.secondary : styles.primary,
        disabled && styles.disabled,
        pressed && !disabled && styles.pressed,
        style,
      ]}
    >
      <View style={styles.inner}>
        {leadingIcon ? <AppIcon name={leadingIcon} size={18} color={iconColor} secondaryColor={iconColor} /> : null}
        <AppText
          variant="bodyStrong"
          style={[styles.label, secondary ? styles.secondaryLabel : styles.primaryLabel]}
        >
          {label}
        </AppText>
        {trailingIcon ? <AppIcon name={trailingIcon} size={18} color={iconColor} secondaryColor={iconColor} /> : null}
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
    paddingHorizontal: spacing.lg,
    borderWidth: 1,
  },
  inner: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: spacing.xs,
  },
  primary: {
    backgroundColor: colors.brand.primary,
    borderColor: colors.brand.primary,
  },
  secondary: {
    backgroundColor: semantic.bg.surface,
    borderColor: semantic.border.soft,
  },
  disabled: {
    opacity: 0.45,
  },
  pressed: {
    transform: [{ scale: 0.96 }],
    opacity: 0.85,
  },
  label: {
    fontFamily: typography.family.bold,
  },
  primaryLabel: {
    color: semantic.text.inverted,
  },
  secondaryLabel: {
    color: semantic.text.primary,
  },
});
