import React from "react";
import Svg, { Circle, Path, Rect } from "react-native-svg";

export type HeaderArtworkVariant = "customer" | "driver" | "trust" | "status";

type HeaderArtworkProps = {
  variant?: HeaderArtworkVariant;
  width?: number;
  height?: number;
};

export function HeaderArtwork({ variant = "trust", width = 180, height = 140 }: HeaderArtworkProps) {
  const primaryLine = variant === "status" ? "#059669" : "#1B6EF3";
  const mutedLine = "#9CA3AF";

  return (
    <Svg width={width} height={height} viewBox="0 0 180 140" fill="none">
      <Rect x="12" y="10" width="156" height="120" rx="18" fill="#FFFFFF" />
      <Rect x="12" y="10" width="156" height="120" rx="18" stroke="rgba(229,231,235,0.6)" />
      <Rect x="30" y="28" width="120" height="18" rx="9" fill="#F3F4F6" />
      <Path d="M44 37H98" stroke={primaryLine} strokeWidth="3.2" strokeLinecap="round" />
      <Path d="M112 37H136" stroke={mutedLine} strokeWidth="3.2" strokeLinecap="round" opacity="0.6" />
      <Rect x="30" y="58" width="54" height="44" rx="12" fill="#F3F4F6" />
      <Rect x="96" y="58" width="54" height="44" rx="12" fill="#F3F4F6" />
      <Path d="M42 72H70" stroke={primaryLine} strokeWidth="3" strokeLinecap="round" />
      <Path d="M42 83H62" stroke={mutedLine} strokeWidth="3" strokeLinecap="round" opacity="0.6" />
      <Path d="M108 72H136" stroke={primaryLine} strokeWidth="3" strokeLinecap="round" />
      <Path d="M108 83H128" stroke={mutedLine} strokeWidth="3" strokeLinecap="round" opacity="0.6" />
      {variant === "driver" ? (
        <>
          <Circle cx="137" cy="98" r="10" fill="#1B6EF3" />
          <Path d="M132 98L136 102L143 94" stroke="#FFFFFF" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
        </>
      ) : null}
      {variant === "status" ? (
        <>
          <Circle cx="137" cy="98" r="10" fill="#059669" />
          <Path d="M132 98L136 102L143 94" stroke="#FFFFFF" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
        </>
      ) : null}
      {variant === "customer" ? (
        <>
          <Path d="M48 112H132" stroke="#1B6EF3" strokeWidth="3.4" strokeLinecap="round" />
          <Circle cx="48" cy="112" r="5" fill="#1B6EF3" />
          <Circle cx="132" cy="112" r="5" fill="#1B6EF3" />
        </>
      ) : null}
    </Svg>
  );
}
