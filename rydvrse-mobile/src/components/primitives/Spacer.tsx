import React from "react";
import { View } from "react-native";

import { space } from "@/theme";

type SpacerProps = {
  size?: keyof typeof space;
  horizontal?: boolean;
};

export function Spacer({ size = 4, horizontal = false }: SpacerProps) {
  const value = space[size];
  return (
    <View
      style={
        horizontal
          ? { width: value, height: 1 }
          : { height: value, width: 1 }
      }
    />
  );
}
