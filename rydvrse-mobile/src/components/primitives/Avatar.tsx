import React from "react";
import { StyleSheet, View } from "react-native";

import { Text } from "@/components/primitives/Text";
import { colors, semantic, space } from "@/theme";

type AvatarProps = {
  name?: string;
  size?: number;
};

function getInitials(name?: string): string {
  if (!name) return "?";
  const parts = name.trim().split(/\s+/);
  if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  return parts[0].substring(0, 2).toUpperCase();
}

export function Avatar({ name, size = 60 }: AvatarProps) {
  const borderRadius = size * 0.5;
  const fontSize = size * 0.36;

  return (
    <View
      style={[
        styles.container,
        { width: size, height: size, borderRadius },
      ]}
      accessibilityRole="image"
      accessibilityLabel={name ? `Avatar for ${name}` : "Avatar"}
    >
      <Text
        variant="bodyStrong"
        style={{ fontSize, lineHeight: fontSize * 1.2, color: semantic.text.inverted }}
      >
        {getInitials(name)}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.brand.primary,
    alignItems: "center",
    justifyContent: "center",
  },
});
