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
      <Rect x="10" y="18" width="120" height="64" rx="16" fill="#FFFFFF" stroke="rgba(34,42,53,0.12)" />
      <Rect x="26" y="32" width="88" height="8" rx="4" fill="#F5F5F5" />
      <Rect x="26" y="48" width="64" height="8" rx="4" fill="#F5F5F5" />
      {variant === "booking" ? (
        <>
          <Path d="M34 68C48 58 65 56 104 62" stroke="#242424" strokeWidth="3.2" strokeLinecap="round" />
          <Circle cx="34" cy="68" r="4.5" fill="#242424" />
          <Circle cx="104" cy="62" r="4.5" fill="#242424" />
        </>
      ) : null}
      {variant === "jobs" ? (
        <>
          <Rect x="48" y="58" width="44" height="16" rx="8" fill="#242424" />
          <Path d="M58 66H82" stroke="#FFFFFF" strokeWidth="2.4" strokeLinecap="round" />
        </>
      ) : null}
      {variant === "support" ? (
        <>
          <Circle cx="72" cy="66" r="12" fill="#242424" />
          <Path d="M72 60V67" stroke="#FFFFFF" strokeWidth="3" strokeLinecap="round" />
          <Circle cx="72" cy="72" r="1.8" fill="#FFFFFF" />
        </>
      ) : null}
    </Svg>
  );
}
