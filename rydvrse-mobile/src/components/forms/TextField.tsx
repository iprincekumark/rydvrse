import React from "react";
import { StyleSheet, TextInput, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, spacing, typography } from "@/theme";

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

export function TextField({
  label,
  value,
  placeholder,
  onChangeText,
  keyboardType = "default",
  multiline = false,
  helperText,
  icon
}: TextFieldProps) {
  return (
    <View style={styles.wrapper}>
      <AppText variant="caption" style={styles.label}>
        {label}
      </AppText>
      <View style={[styles.inputShell, multiline && styles.multilineShell]}>
        {icon ? (
          <View style={styles.iconWrap}>
            <AppIcon name={icon} size={17} color={colors.primary.base} secondaryColor={colors.secondary.muted} />
          </View>
        ) : null}
        <TextInput
          value={value}
          placeholder={placeholder}
          placeholderTextColor={colors.text.muted}
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
    gap: spacing.xs
  },
  label: {
    color: colors.text.secondary
  },
  inputShell: {
    minHeight: 54,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border.soft,
    backgroundColor: colors.background.surface,
    flexDirection: "row",
    alignItems: "center",
    overflow: "hidden"
  },
  multilineShell: {
    alignItems: "flex-start"
  },
  iconWrap: {
    width: 46,
    alignItems: "center",
    justifyContent: "center",
    paddingTop: spacing.xs,
    opacity: 0.72
  },
  input: {
    flex: 1,
    minHeight: 54,
    paddingHorizontal: spacing.md,
    color: colors.text.primary,
    fontFamily: typography.family.medium,
    fontSize: 15
  },
  inputWithIcon: {
    paddingLeft: 0
  },
  multiline: {
    minHeight: 110,
    paddingTop: spacing.md,
    textAlignVertical: "top"
  },
  helperText: {
    color: colors.state.warning
  }
});
