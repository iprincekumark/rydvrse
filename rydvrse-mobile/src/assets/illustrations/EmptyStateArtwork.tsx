import React from "react";
import Svg, { Circle, Path, Rect } from "react-native-svg";

export type EmptyStateArtworkVariant = "booking" | "jobs" | "support";

type EmptyStateArtworkProps = {
  variant?: EmptyStateArtworkVariant;
  width?: number;
  height?: number;
};

export function EmptyStateArtwork({ variant = "booking", width = 140, height = 96 }: EmptyStateArtworkProps) {
  return (
    <Svg width={width} height={height} viewBox="0 0 140 96" fill="none">
      <Rect x="10" y="18" width="120" height="64" rx="24" fill="#EEF7F4" />
      <Circle cx="36" cy="66" r="16" fill="#FFFFFF" />
      <Circle cx="102" cy="30" r="12" fill="#FFFFFF" opacity="0.8" />
      {variant === "booking" ? (
        <>
          <Path d="M34 63C46 47 61 39 96 41" stroke="#0C6D69" strokeWidth="3.5" strokeLinecap="round" />
          <Circle cx="34" cy="63" r="4.5" fill="#C8842F" />
          <Circle cx="100" cy="41" r="4.5" fill="#0C6D69" />
        </>
      ) : null}
      {variant === "jobs" ? (
        <>
          <Rect x="50" y="34" width="42" height="26" rx="13" fill="#FFFFFF" />
          <Path d="M58 47H84" stroke="#0C6D69" strokeWidth="3.2" strokeLinecap="round" />
          <Path d="M58 53H74" stroke="#C8842F" strokeWidth="3.2" strokeLinecap="round" />
        </>
      ) : null}
      {variant === "support" ? (
        <>
          <Circle cx="72" cy="48" r="18" fill="#FFFFFF" />
          <Path d="M72 38V50" stroke="#0C6D69" strokeWidth="4" strokeLinecap="round" />
          <Circle cx="72" cy="58" r="2.5" fill="#C8842F" />
        </>
      ) : null}
    </Svg>
  );
}
