import React, { useState } from "react";
import { StyleSheet, TextInput, View } from "react-native";

import { AppIcon, AppIconName } from "@/assets/icons/AppIcon";
import { Text } from "@/components/primitives/Text";
import { colors, fontFamily, radius, semantic, space } from "@/theme";

type TextFieldProps = {
  label: string;
  value: string;
  placeholder?: string;
  onChangeText?: (value: string) => void;
  keyboardType?: "default" | "numeric" | "phone-pad" | "email-address";
  multiline?: boolean;
  helperText?: string;
  errorText?: string;
  icon?: AppIconName;
  editable?: boolean;
};

export function TextField({
  label,
  value,
  placeholder,
  onChangeText,
  keyboardType = "default",
  multiline = false,
  helperText,
  errorText,
  icon,
  editable = true,
}: TextFieldProps) {
  const [focused, setFocused] = useState(false);
  const hasError = Boolean(errorText);

  return (
    <View style={styles.wrapper}>
      <Text variant="label" style={styles.label}>{label}</Text>
      <View
        style={[
          styles.inputShell,
          multiline && styles.multilineShell,
          focused && styles.focusedShell,
          hasError && styles.errorShell,
        ]}
      >
        {icon ? (
          <View style={styles.iconWrap}>
            <AppIcon
              name={icon}
              size={17}
              color={focused ? colors.brand.primary : colors.neutral[400]}
              secondaryColor={colors.neutral[400]}
            />
          </View>
        ) : null}
        <TextInput
          value={value}
          placeholder={placeholder}
          placeholderTextColor={colors.neutral[400]}
          onChangeText={onChangeText}
          keyboardType={keyboardType}
          multiline={multiline}
          editable={editable}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          style={[
            styles.input,
            multiline && styles.multiline,
            icon && styles.inputWithIcon,
          ]}
        />
      </View>
      {hasError ? (
        <Text variant="caption" style={styles.errorText}>{errorText}</Text>
      ) : helperText ? (
        <Text variant="caption" style={styles.helperText}>{helperText}</Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    gap: space[2],
  },
  label: {
    color: semantic.text.secondary,
  },
  inputShell: {
    minHeight: 52,
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
  focusedShell: {
    borderWidth: 2,
    borderColor: colors.brand.primary,
  },
  errorShell: {
    borderWidth: 2,
    borderColor: colors.state.danger,
  },
  iconWrap: {
    width: 46,
    alignItems: "center",
    justifyContent: "center",
    paddingTop: space[2],
    opacity: 0.85,
  },
  input: {
    flex: 1,
    minHeight: 52,
    paddingHorizontal: space[4],
    color: semantic.text.primary,
    fontFamily: fontFamily.medium,
    fontSize: 15,
  },
  inputWithIcon: {
    paddingLeft: 0,
  },
  multiline: {
    minHeight: 110,
    paddingTop: space[4],
    textAlignVertical: "top",
  },
  helperText: {
    color: semantic.text.muted,
  },
  errorText: {
    color: colors.state.danger,
  },
});
