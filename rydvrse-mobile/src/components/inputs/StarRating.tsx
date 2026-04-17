import React from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { colors, space } from "@/theme";

type StarRatingProps = {
  value: number;
  onChange?: (rating: number) => void;
  size?: number;
};

export function StarRating({ value, onChange, size = 32 }: StarRatingProps) {
  return (
    <View style={styles.container}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Pressable
          key={star}
          onPress={() => onChange?.(star)}
          accessibilityRole="button"
          accessibilityLabel={`${star} star${star > 1 ? "s" : ""}`}
          accessibilityState={{ selected: star <= value }}
          style={styles.star}
        >
          <AppIcon
            name="star"
            size={size}
            color={star <= value ? colors.accent.primary : colors.neutral[300]}
            secondaryColor={star <= value ? colors.accent.primary : colors.neutral[300]}
          />
        </Pressable>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    gap: space[2],
  },
  star: {
    padding: space[1],
  },
});
