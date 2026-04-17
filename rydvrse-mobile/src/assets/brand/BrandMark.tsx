import React from "react";
import Svg, { Circle, Path } from "react-native-svg";

type BrandMarkProps = {
  size?: number;
};

export function BrandMark({ size = 52 }: BrandMarkProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 64 64" fill="none">
      <Circle cx="32" cy="32" r="29" fill="#1B6EF3" />
      <Circle cx="32" cy="32" r="20" fill="#FFFFFF" />
      <Path
        d="M23 38.5C26.5 35.5 28.7 28.5 35.5 28.5C39.7 28.5 42.2 31 44.5 33.5"
        stroke="#1B6EF3"
        strokeWidth="4.5"
        strokeLinecap="round"
      />
      <Circle cx="21" cy="41" r="4" fill="#1B6EF3" />
      <Circle cx="46" cy="34" r="4" fill="#1B6EF3" />
      <Path
        d="M25 23.5C27.6 20.3 31.7 18.5 36.4 18.5C40.9 18.5 44.3 19.7 47.5 22.5"
        stroke="#1B6EF3"
        strokeWidth="3"
        strokeLinecap="round"
        opacity="0.35"
      />
    </Svg>
  );
}
