import React from "react";
import { StyleSheet, TextInput, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, fontFamily, radius, semantic, spacing, typography } from "@/theme";

type TextFieldProps = {
  label: string;
  value: string;
  placeholder?: string;
  onChangeText?: (value: string) => void;
  keyboardType?: "default" | "numeric" | "phone-pad";
  multiline?: boolean;
  helperText?: string;
  icon?: AppIconName;
};

/**
 * Legacy TextField — updated with new tokens.
 * Prefer the new inputs/TextField.tsx for new code.
 */
export function TextField({
  label,
  value,
  placeholder,
  onChangeText,
  keyboardType = "default",
  multiline = false,
  helperText,
  icon,
}: TextFieldProps) {
  return (
    <View style={styles.wrapper}>
      <AppText variant="caption" style={styles.label}>
        {label}
      </AppText>
      <View style={[styles.inputShell, multiline && styles.multilineShell]}>
        {icon ? (
          <View style={styles.iconWrap}>
            <AppIcon name={icon} size={17} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
          </View>
        ) : null}
        <TextInput
          value={value}
          placeholder={placeholder}
          placeholderTextColor={colors.neutral[400]}
          onChangeText={onChangeText}
          keyboardType={keyboardType}
          multiline={multiline}
          style={[styles.input, multiline && styles.multiline, icon && styles.inputWithIcon]}
        />
      </View>
      {helperText ? (
        <AppText variant="caption" style={styles.helperText}>
          {helperText}
        </AppText>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: spacing.xs,
  },
  label: {
    color: semantic.text.secondary,
  },
  inputShell: {
    minHeight: 46,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    backgroundColor: semantic.bg.surface,
    flexDirection: "row",
    alignItems: "center",
    overflow: "hidden",
  },
  multilineShell: {
    alignItems: "flex-start",
  },
  iconWrap: {
    width: 40,
    alignItems: "center",
    justifyContent: "center",
    paddingTop: spacing.xs,
    opacity: 0.85,
  },
  input: {
    flex: 1,
    minHeight: 46,
    paddingHorizontal: spacing.md,
    color: semantic.text.primary,
    fontFamily: fontFamily.medium,
    fontSize: 14,
  },
  inputWithIcon: {
    paddingLeft: 0,
  },
  multiline: {
    minHeight: 78,
    paddingTop: spacing.sm,
    textAlignVertical: "top",
  },
  helperText: {
    color: semantic.text.muted,
  },
});
