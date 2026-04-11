import React, { useEffect, useRef } from "react";
import { Animated, Easing, StyleSheet, View } from "react-native";
import { LinearGradient } from "expo-linear-gradient";

import { colors, radius } from "@/theme";

export function Skeleton({ height = 16, width = "100%", radiusValue = radius.md }: { height?: number; width?: number | `${number}%`; radiusValue?: number }) {
  const translateX = useRef(new Animated.Value(-1)).current;

  useEffect(() => {
    Animated.loop(
      Animated.timing(translateX, {
        toValue: 1,
        duration: 1400,
        easing: Easing.linear,
        useNativeDriver: true
      })
    ).start();
  }, [translateX]);

  return (
    <View style={[styles.base, { height, width, borderRadius: radiusValue }]}>
      <Animated.View
        style={[
          StyleSheet.absoluteFillObject,
          {
            transform: [
              {
                translateX: translateX.interpolate({
                  inputRange: [-1, 1],
                  outputRange: [-220, 220]
                })
              }
            ]
          }
        ]}
      >
        <LinearGradient
          colors={["rgba(255,255,255,0)", "rgba(255,255,255,0.7)", "rgba(255,255,255,0)"]}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 0 }}
          style={styles.gradient}
        />
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  base: {
    overflow: "hidden",
    backgroundColor: colors.background.muted
  },
  gradient: {
    flex: 1,
    width: 180
  }
});
