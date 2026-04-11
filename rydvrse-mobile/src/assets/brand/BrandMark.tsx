import React from "react";
import Svg, { Circle, Defs, LinearGradient, Path, Stop } from "react-native-svg";

type BrandMarkProps = {
  size?: number;
};

export function BrandMark({ size = 52 }: BrandMarkProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 64 64" fill="none">
      <Defs>
        <LinearGradient id="brand-shell" x1="10" y1="8" x2="54" y2="56" gradientUnits="userSpaceOnUse">
          <Stop stopColor="#0C6D69" />
          <Stop offset="1" stopColor="#1A8F89" />
        </LinearGradient>
        <LinearGradient id="brand-core" x1="14" y1="12" x2="44" y2="42" gradientUnits="userSpaceOnUse">
          <Stop stopColor="#EAF7F3" />
          <Stop offset="1" stopColor="#C5ECE2" />
        </LinearGradient>
      </Defs>
      <Circle cx="32" cy="32" r="29" fill="url(#brand-shell)" />
      <Circle cx="32" cy="32" r="21" fill="url(#brand-core)" />
      <Path
        d="M23 38.5C26.5 35.5 28.7 28.5 35.5 28.5C39.7 28.5 42.2 31 44.5 33.5"
        stroke="#0C6D69"
        strokeWidth="4.5"
        strokeLinecap="round"
      />
      <Circle cx="21" cy="41" r="4" fill="#C8842F" />
      <Circle cx="46" cy="34" r="4" fill="#0C6D69" />
      <Path
        d="M25 23.5C27.6 20.3 31.7 18.5 36.4 18.5C40.9 18.5 44.3 19.7 47.5 22.5"
        stroke="#0C6D69"
        strokeWidth="3"
        strokeLinecap="round"
        opacity="0.35"
      />
    </Svg>
  );
}
