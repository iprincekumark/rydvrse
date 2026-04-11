import React from "react";
import Svg, { Circle, Defs, LinearGradient, Path, Rect, Stop } from "react-native-svg";

export type HeaderArtworkVariant = "customer" | "driver" | "trust" | "status";

type HeaderArtworkProps = {
  variant?: HeaderArtworkVariant;
  width?: number;
  height?: number;
};

export function HeaderArtwork({ variant = "trust", width = 180, height = 140 }: HeaderArtworkProps) {
  const glow = variant === "driver" ? "#D8F4E8" : variant === "status" ? "#FFF0D8" : "#DDF2EE";
  const accent = variant === "driver" ? "#1E8E5C" : "#C8842F";

  return (
    <Svg width={width} height={height} viewBox="0 0 180 140" fill="none">
      <Defs>
        <LinearGradient id="panel" x1="14" y1="16" x2="160" y2="124" gradientUnits="userSpaceOnUse">
          <Stop stopColor="#F6FBF9" />
          <Stop offset="1" stopColor={glow} />
        </LinearGradient>
      </Defs>
      <Rect x="12" y="10" width="156" height="120" rx="30" fill="url(#panel)" />
      <Circle cx="132" cy="34" r="18" fill="#FFFFFF" opacity="0.72" />
      <Circle cx="42" cy="108" r="24" fill="#FFFFFF" opacity="0.5" />
      <Path d="M44 93C63 58 92 48 132 54" stroke="#0C6D69" strokeWidth="4" strokeLinecap="round" />
      <Circle cx="42" cy="93" r="6" fill={accent} />
      <Circle cx="136" cy="54" r="6" fill="#0C6D69" />
      <Rect x="32" y="28" width="58" height="28" rx="14" fill="#FFFFFF" />
      <Path d="M46 42H76" stroke="#0C6D69" strokeWidth="3.2" strokeLinecap="round" />
      <Path d="M46 49H67" stroke="#0C6D69" strokeWidth="3.2" strokeLinecap="round" opacity="0.45" />
      {variant === "driver" ? (
        <>
          <Rect x="102" y="74" width="42" height="28" rx="14" fill="#FFFFFF" />
          <Path d="M114 89H132" stroke={accent} strokeWidth="3.2" strokeLinecap="round" />
        </>
      ) : null}
      {variant === "status" ? (
        <>
          <Circle cx="128" cy="84" r="15" fill="#FFFFFF" />
          <Path d="M122 84L126.5 88.5L135.5 79.5" stroke="#1E8E5C" strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round" />
        </>
      ) : null}
      {variant === "customer" ? (
        <>
          <Rect x="96" y="74" width="50" height="30" rx="15" fill="#FFFFFF" />
          <Path d="M108 89H133" stroke={accent} strokeWidth="3.2" strokeLinecap="round" />
          <Circle cx="124" cy="89" r="3.5" fill="#0C6D69" />
        </>
      ) : null}
    </Svg>
  );
}
