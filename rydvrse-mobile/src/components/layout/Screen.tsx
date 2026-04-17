import React, { PropsWithChildren } from "react";
import { ScrollView, StyleSheet, View, useWindowDimensions } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { semantic, space } from "@/theme";

type ScreenVariant = "default" | "map" | "hero";

type ScreenProps = PropsWithChildren<{
  padded?: boolean;
  scrollable?: boolean;
  backgroundColor?: string;
  variant?: ScreenVariant;
}>;

export function Screen({
  children,
  padded = true,
  scrollable = true,
  backgroundColor = semantic.bg.app,
  variant = "default",
}: ScreenProps) {
  const { width } = useWindowDimensions();
  const maxWidth = width >= 1100 ? 920 : width >= 720 ? 680 : undefined;
  const horizontalPadding = width >= 720 ? space[7] : space[5];
  const verticalPadding = width >= 720 ? space[8] : space[6];

  const content = (
    <View
      style={[
        styles.content,
        padded && {
          paddingHorizontal: horizontalPadding,
          paddingVertical: variant === "map" ? 0 : verticalPadding,
        },
      ]}
    >
      <View
        style={[
          styles.inner,
          maxWidth ? { maxWidth, alignSelf: "center", width: "100%" } : null,
        ]}
      >
        {children}
      </View>
    </View>
  );

  return (
    <SafeAreaView
      style={[styles.safeArea, { backgroundColor }]}
      edges={variant === "map" ? ["left", "right"] : undefined}
    >
      {scrollable ? (
        <ScrollView contentContainerStyle={styles.scroll}>{content}</ScrollView>
      ) : (
        content
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
  },
  scroll: {
    flexGrow: 1,
  },
  content: {
    flex: 1,
  },
  inner: {
    flex: 1,
  },
});
