import React from "react";
import Svg, { Circle, Defs, LinearGradient, Path, Rect, Stop } from "react-native-svg";

type RouteArtworkProps = {
  width?: number;
  height?: number;
};

export function RouteArtwork({ width = 320, height = 180 }: RouteArtworkProps) {
  return (
    <Svg width={width} height={height} viewBox="0 0 320 180" fill="none">
      <Defs>
        <LinearGradient id="route-bg" x1="20" y1="10" x2="260" y2="180" gradientUnits="userSpaceOnUse">
          <Stop stopColor="#EDF8F4" />
          <Stop offset="1" stopColor="#D9ECE7" />
        </LinearGradient>
      </Defs>
      <Rect width="320" height="180" rx="30" fill="url(#route-bg)" />
      <Path d="M28 38H292" stroke="#FFFFFF" strokeWidth="10" strokeLinecap="round" opacity="0.55" />
      <Path d="M28 88H292" stroke="#FFFFFF" strokeWidth="10" strokeLinecap="round" opacity="0.45" />
      <Path d="M28 138H292" stroke="#FFFFFF" strokeWidth="10" strokeLinecap="round" opacity="0.35" />
      <Path d="M60 126C96 84 138 80 194 92C228 100 248 86 270 58" stroke="#0C6D69" strokeWidth="5" strokeLinecap="round" />
      <Circle cx="58" cy="126" r="10" fill="#C8842F" />
      <Circle cx="272" cy="58" r="10" fill="#0C6D69" />
      <Circle cx="182" cy="92" r="6" fill="#FFFFFF" stroke="#0C6D69" strokeWidth="3" />
      <Rect x="178" y="26" width="84" height="32" rx="16" fill="#FFFFFF" />
      <Path d="M194 42H246" stroke="#0C6D69" strokeWidth="3.5" strokeLinecap="round" />
      <Path d="M194 48H225" stroke="#0C6D69" strokeWidth="3.5" strokeLinecap="round" opacity="0.35" />
    </Svg>
  );
}
