import React from "react";
import Svg, { Circle, Path, Rect } from "react-native-svg";

export type HeaderArtworkVariant = "customer" | "driver" | "trust" | "status";

type HeaderArtworkProps = {
  variant?: HeaderArtworkVariant;
  width?: number;
  height?: number;
};

export function HeaderArtwork({ variant = "trust", width = 180, height = 140 }: HeaderArtworkProps) {
  const primaryLine = variant === "status" ? "#167447" : "#242424";
  const mutedLine = "#898989";

  return (
    <Svg width={width} height={height} viewBox="0 0 180 140" fill="none">
      <Rect x="12" y="10" width="156" height="120" rx="18" fill="#FFFFFF" />
      <Rect x="12" y="10" width="156" height="120" rx="18" stroke="rgba(34,42,53,0.12)" />
      <Rect x="30" y="28" width="120" height="18" rx="9" fill="#F5F5F5" />
      <Path d="M44 37H98" stroke={primaryLine} strokeWidth="3.2" strokeLinecap="round" />
      <Path d="M112 37H136" stroke={mutedLine} strokeWidth="3.2" strokeLinecap="round" opacity="0.6" />
      <Rect x="30" y="58" width="54" height="44" rx="12" fill="#F5F5F5" />
      <Rect x="96" y="58" width="54" height="44" rx="12" fill="#F5F5F5" />
      <Path d="M42 72H70" stroke={primaryLine} strokeWidth="3" strokeLinecap="round" />
      <Path d="M42 83H62" stroke={mutedLine} strokeWidth="3" strokeLinecap="round" opacity="0.6" />
      <Path d="M108 72H136" stroke={primaryLine} strokeWidth="3" strokeLinecap="round" />
      <Path d="M108 83H128" stroke={mutedLine} strokeWidth="3" strokeLinecap="round" opacity="0.6" />
      {variant === "driver" ? (
        <>
          <Circle cx="137" cy="98" r="10" fill="#242424" />
          <Path d="M132 98L136 102L143 94" stroke="#FFFFFF" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
        </>
      ) : null}
      {variant === "status" ? (
        <>
          <Circle cx="137" cy="98" r="10" fill="#167447" />
          <Path d="M132 98L136 102L143 94" stroke="#FFFFFF" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
        </>
      ) : null}
      {variant === "customer" ? (
        <>
          <Path d="M48 112H132" stroke="#242424" strokeWidth="3.4" strokeLinecap="round" />
          <Circle cx="48" cy="112" r="5" fill="#242424" />
          <Circle cx="132" cy="112" r="5" fill="#242424" />
        </>
      ) : null}
    </Svg>
  );
}
