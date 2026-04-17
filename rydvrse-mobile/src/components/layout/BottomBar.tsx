import React from "react";
import { StyleSheet, View, useWindowDimensions } from "react-native";

import { AppIconName } from "@/assets/icons/AppIcon";
import { Button } from "@/components/primitives/Button";
import { space } from "@/theme";

type BottomBarProps = {
  primaryLabel: string;
  secondaryLabel?: string;
  onPrimaryPress?: () => void;
  onSecondaryPress?: () => void;
  primaryDisabled?: boolean;
  primaryLoading?: boolean;
  primaryIcon?: AppIconName;
  secondaryIcon?: AppIconName;
  primaryVariant?: "primary" | "danger";
};

export function BottomBar({
  primaryLabel,
  secondaryLabel,
  onPrimaryPress,
  onSecondaryPress,
  primaryDisabled,
  primaryLoading = false,
  primaryIcon = "arrowRight",
  secondaryIcon,
  primaryVariant = "primary",
}: BottomBarProps) {
  const { width } = useWindowDimensions();
  const stacked = width < 390;

  return (
    <View style={[styles.container, stacked && styles.containerStacked]}>
      {secondaryLabel ? (
        <Button
          label={secondaryLabel}
          variant="secondary"
          onPress={onSecondaryPress}
          leadingIcon={secondaryIcon}
          style={styles.flex}
        />
      ) : null}
      <Button
        label={primaryLabel}
        variant={primaryVariant}
        onPress={onPrimaryPress}
        disabled={primaryDisabled}
        loading={primaryLoading}
        trailingIcon={primaryIcon}
        style={styles.flex}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    gap: space[3],
    paddingTop: space[4],
  },
  containerStacked: {
    flexDirection: "column",
  },
  flex: {
    flex: 1,
  },
});
