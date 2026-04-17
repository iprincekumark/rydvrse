import React from "react";
import Svg, { Circle, Path, Rect } from "react-native-svg";

type RouteArtworkProps = {
  width?: number;
  height?: number;
};

export function RouteArtwork({ width = 320, height = 180 }: RouteArtworkProps) {
  return (
    <Svg width={width} height={height} viewBox="0 0 320 180" fill="none">
      <Rect width="320" height="180" rx="16" fill="#FFFFFF" />
      <Rect x="0.5" y="0.5" width="319" height="179" rx="15.5" stroke="rgba(229,231,235,0.6)" />
      <Path d="M28 38H292" stroke="#F3F4F6" strokeWidth="10" strokeLinecap="round" />
      <Path d="M28 88H292" stroke="#F3F4F6" strokeWidth="10" strokeLinecap="round" />
      <Path d="M28 138H292" stroke="#F3F4F6" strokeWidth="10" strokeLinecap="round" />
      <Path d="M60 126C96 84 138 80 194 92C228 100 248 86 270 58" stroke="#1B6EF3" strokeWidth="5" strokeLinecap="round" />
      <Circle cx="58" cy="126" r="9" fill="#1B6EF3" />
      <Circle cx="272" cy="58" r="9" fill="#1B6EF3" />
      <Circle cx="182" cy="92" r="6" fill="#FFFFFF" stroke="#1B6EF3" strokeWidth="3" />
      <Rect x="178" y="26" width="84" height="32" rx="12" fill="#F3F4F6" />
      <Path d="M194 42H246" stroke="#1B6EF3" strokeWidth="3.5" strokeLinecap="round" />
      <Path d="M194 48H225" stroke="#9CA3AF" strokeWidth="3.5" strokeLinecap="round" opacity="0.55" />
    </Svg>
  );
}
