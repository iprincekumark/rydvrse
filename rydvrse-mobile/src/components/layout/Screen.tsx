import React, { PropsWithChildren } from "react";
import { ScrollView, StyleSheet, View, useWindowDimensions } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import { colors, spacing } from "@/theme";

type ScreenProps = PropsWithChildren<{
  padded?: boolean;
  scrollable?: boolean;
  backgroundColor?: string;
}>;

export function Screen({ children, padded = true, scrollable = true, backgroundColor = colors.background.app }: ScreenProps) {
  const { width } = useWindowDimensions();
  const maxWidth = width >= 1100 ? 920 : width >= 720 ? 680 : undefined;
  const horizontalPadding = width >= 720 ? spacing.xxl : spacing.lg;

  const content = (
    <View style={[styles.content, padded && styles.padded, padded && { paddingHorizontal: horizontalPadding }]}>
      <View style={[styles.inner, maxWidth ? { maxWidth, alignSelf: "center", width: "100%" } : null]}>
        {children}
      </View>
    </View>
  );

  return (
    <SafeAreaView style={[styles.safeArea, { backgroundColor }]}>
      {scrollable ? <ScrollView contentContainerStyle={styles.scroll}>{content}</ScrollView> : content}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1
  },
  scroll: {
    flexGrow: 1
  },
  content: {
    flex: 1
  },
  inner: {
    flex: 1
  },
  padded: {
    paddingVertical: spacing.md
  }
});
