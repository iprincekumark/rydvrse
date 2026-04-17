import React from "react";
import { Pressable, StyleSheet, View, ViewStyle } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, spacing, typography } from "@/theme";

type PrimaryButtonProps = {
  label: string;
  onPress?: () => void;
  secondary?: boolean;
  disabled?: boolean;
  style?: ViewStyle;
  leadingIcon?: AppIconName;
  trailingIcon?: AppIconName;
};

export function PrimaryButton({
  label,
  onPress,
  secondary = false,
  disabled = false,
  style,
  leadingIcon,
  trailingIcon
}: PrimaryButtonProps) {
  const iconColor = secondary ? colors.text.primary : colors.text.inverted;

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
        style
      ]}
    >
      <View style={styles.inner}>
        {leadingIcon ? <AppIcon name={leadingIcon} size={18} color={iconColor} secondaryColor={iconColor} /> : null}
        <AppText
          variant="bodyStrong"
          style={[
            styles.label,
            secondary ? styles.secondaryLabel : styles.primaryLabel
          ]}
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
    minHeight: 54,
    borderRadius: radius.md,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: spacing.lg,
    borderWidth: 1
  },
  inner: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: spacing.xs
  },
  primary: {
    backgroundColor: colors.primary.base,
    borderColor: colors.primary.base
  },
  secondary: {
    backgroundColor: colors.background.surface,
    borderColor: colors.border.soft
  },
  disabled: {
    opacity: 0.45
  },
  pressed: {
    transform: [{ scale: 0.985 }],
    opacity: 0.92
  },
  label: {
    fontFamily: typography.family.bold
  },
  primaryLabel: {
    color: colors.text.inverted
  },
  secondaryLabel: {
    color: colors.text.primary
  }
});
