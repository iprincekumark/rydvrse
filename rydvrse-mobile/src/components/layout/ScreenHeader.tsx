import React, { ReactNode } from "react";
import { Pressable, StyleSheet, View } from "react-native";
import { useNavigation } from "@react-navigation/native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, semantic, spacing } from "@/theme";

type ScreenHeaderProps = {
  title: string;
  subtitle?: string;
  onBack?: () => void;
  rightSlot?: ReactNode;
  showBack?: boolean;
};

/**
 * Standard top bar used on every secondary screen. Top-left back button,
 * centred title, optional right slot. Falls back to navigation.goBack() when
 * `onBack` is not provided so callers rarely need to wire it explicitly.
 */
export function ScreenHeader({
  title,
  subtitle,
  onBack,
  rightSlot,
  showBack = true,
}: ScreenHeaderProps) {
  const navigation = useNavigation<any>();

  const handleBack = () => {
    if (onBack) {
      onBack();
      return;
    }
    if (navigation.canGoBack()) {
      navigation.goBack();
    }
  };

  return (
    <View style={styles.container}>
      {showBack ? (
        <Pressable
          onPress={handleBack}
          style={({ pressed }) => [styles.backButton, pressed && { opacity: 0.8 }]}
          accessibilityRole="button"
          accessibilityLabel="Go back"
          hitSlop={8}
        >
          <AppIcon
            name="arrowLeft"
            size={20}
            color={semantic.text.primary}
            secondaryColor={colors.brand.strong}
          />
        </Pressable>
      ) : (
        <View style={styles.backButtonPlaceholder} />
      )}
      <View style={styles.titleBlock}>
        <AppText variant="section" numberOfLines={1}>
          {title}
        </AppText>
        {subtitle ? (
          <AppText variant="caption" numberOfLines={1}>
            {subtitle}
          </AppText>
        ) : null}
      </View>
      <View style={styles.rightSlot}>{rightSlot}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    marginBottom: spacing.md,
    minHeight: 44,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  backButtonPlaceholder: {
    width: 40,
    height: 40,
  },
  titleBlock: {
    flex: 1,
    gap: 2,
  },
  rightSlot: {
    minWidth: 40,
    alignItems: "flex-end",
  },
});
