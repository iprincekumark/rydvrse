import React from "react";
import { StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { Button } from "@/components/primitives/Button";
import { Text } from "@/components/primitives/Text";
import { colors, semantic, space } from "@/theme";

type ErrorStateProps = {
  title?: string;
  message?: string;
  onRetry?: () => void;
  variant?: "network" | "server" | "generic";
};

const variants = {
  network: {
    icon: "status" as const,
    defaultTitle: "No internet connection",
    defaultMessage: "Check your network settings and try again.",
  },
  server: {
    icon: "alert" as const,
    defaultTitle: "Something went wrong",
    defaultMessage: "We're working on it. Please try again in a moment.",
  },
  generic: {
    icon: "alert" as const,
    defaultTitle: "Couldn't load data",
    defaultMessage: "Something didn't work. Tap below to try again.",
  },
};

export function ErrorState({ title, message, onRetry, variant = "generic" }: ErrorStateProps) {
  const v = variants[variant];

  return (
    <View style={styles.container}>
      <View style={styles.iconWrap}>
        <AppIcon name={v.icon} size={32} color={colors.state.danger} secondaryColor={colors.state.danger} />
      </View>
      <Text variant="headingSm" style={styles.title}>{title ?? v.defaultTitle}</Text>
      <Text variant="bodySm" style={styles.message}>{message ?? v.defaultMessage}</Text>
      {onRetry ? (
        <Button label="Try again" variant="secondary" onPress={onRetry} leadingIcon="arrowRight" />
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    padding: space[8],
    gap: space[3],
  },
  iconWrap: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: colors.state.dangerSoft,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: space[2],
  },
  title: {
    textAlign: "center",
  },
  message: {
    textAlign: "center",
    color: semantic.text.muted,
    maxWidth: 300,
  },
});
