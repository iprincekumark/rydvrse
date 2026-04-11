import React from "react";
import { StyleSheet, View, useWindowDimensions } from "react-native";

import { AppIconName } from "@/assets/icons/AppIcon";
import { PrimaryButton } from "@/components/common/PrimaryButton";
import { colors, spacing } from "@/theme";

type BottomActionBarProps = {
  primaryLabel: string;
  secondaryLabel?: string;
  onPrimaryPress?: () => void;
  onSecondaryPress?: () => void;
  primaryDisabled?: boolean;
  primaryIcon?: AppIconName;
  secondaryIcon?: AppIconName;
};

export function BottomActionBar({
  primaryLabel,
  secondaryLabel,
  onPrimaryPress,
  onSecondaryPress,
  primaryDisabled,
  primaryIcon = "arrowRight",
  secondaryIcon
}: BottomActionBarProps) {
  const { width } = useWindowDimensions();
  const stacked = width < 390;

  return (
    <View style={[styles.container, stacked && styles.containerStacked]}>
      {secondaryLabel ? (
        <PrimaryButton label={secondaryLabel} secondary onPress={onSecondaryPress} leadingIcon={secondaryIcon} style={styles.secondary} />
      ) : null}
      <PrimaryButton label={primaryLabel} onPress={onPrimaryPress} disabled={primaryDisabled} trailingIcon={primaryIcon} style={styles.primary} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    gap: spacing.sm,
    paddingTop: spacing.md
  },
  containerStacked: {
    flexDirection: "column"
  },
  secondary: {
    flex: 1
  },
  primary: {
    flex: 1,
    backgroundColor: colors.primary.base
  }
});
