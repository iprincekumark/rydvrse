import React from "react";
import Svg, { Circle, Path, Rect } from "react-native-svg";

import { CustomerServiceType } from "@/constants/serviceTypes";

type ServiceIconProps = {
  serviceType: CustomerServiceType;
  size?: number;
};

export function ServiceIcon({ serviceType, size = 48 }: ServiceIconProps) {
  const stroke = "#242424";
  const accent = "#898989";

  return (
    <Svg width={size} height={size} viewBox="0 0 48 48" fill="none">
      <Rect x="4" y="4" width="40" height="40" rx="12" fill="#F5F5F5" />
      {serviceType === "SCHEDULED_LOCAL" ? (
        <>
          <Circle cx="24" cy="24" r="8.5" stroke={stroke} strokeWidth="2.4" />
          <Circle cx="24" cy="24" r="2.4" fill={stroke} />
          <Path d="M24 12V16.5" stroke={accent} strokeWidth="2.4" strokeLinecap="round" />
          <Path d="M24 31.5V36" stroke={accent} strokeWidth="2.4" strokeLinecap="round" />
          <Path d="M12 24H16.5" stroke={accent} strokeWidth="2.4" strokeLinecap="round" />
          <Path d="M31.5 24H36" stroke={accent} strokeWidth="2.4" strokeLinecap="round" />
        </>
      ) : null}
      {serviceType === "ONE_WAY_DROP" ? (
        <>
          <Path d="M13 31C15.7 22.1 21.1 17 31.5 17" stroke={stroke} strokeWidth="2.8" strokeLinecap="round" />
          <Path d="M27.5 13L33.5 17L27.5 21" stroke={stroke} strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round" />
          <Circle cx="14" cy="31" r="2.8" fill={accent} />
          <Circle cx="33" cy="17" r="2.8" fill={stroke} />
        </>
      ) : null}
      {serviceType === "ROUND_TRIP" ? (
        <>
          <Path d="M16 17C18.8 14.8 22 13.8 26 13.8C31 13.8 34.3 15.7 36 18.8" stroke={stroke} strokeWidth="2.6" strokeLinecap="round" />
          <Path d="M36 18.8V13.5" stroke={stroke} strokeWidth="2.6" strokeLinecap="round" />
          <Path d="M36 18.8H30.5" stroke={stroke} strokeWidth="2.6" strokeLinecap="round" />
          <Path d="M32 31C29.2 33.2 26 34.2 22 34.2C17 34.2 13.7 32.3 12 29.2" stroke={accent} strokeWidth="2.6" strokeLinecap="round" />
          <Path d="M12 29.2V34.5" stroke={accent} strokeWidth="2.6" strokeLinecap="round" />
          <Path d="M12 29.2H17.5" stroke={accent} strokeWidth="2.6" strokeLinecap="round" />
        </>
      ) : null}
      {serviceType === "AIRPORT" ? (
        <>
          <Path d="M15 28L33 20.5" stroke={stroke} strokeWidth="2.8" strokeLinecap="round" />
          <Path d="M26.5 14L33 20.5L26 22" stroke={stroke} strokeWidth="2.8" strokeLinecap="round" strokeLinejoin="round" />
          <Path d="M20 24L16 17" stroke={accent} strokeWidth="2.6" strokeLinecap="round" />
          <Path d="M22 30L17 33" stroke={accent} strokeWidth="2.6" strokeLinecap="round" />
        </>
      ) : null}
      {serviceType === "LATE_NIGHT_SAFE_RETURN" ? (
        <>
          <Path
            d="M28.8 14.5C24.3 15.3 21 19.2 21 23.9C21 28.9 24.9 33 29.8 33C32.4 33 34.8 31.9 36.4 30.1C35.1 33.9 31.5 36.6 27.2 36.6C21.8 36.6 17.4 32.2 17.4 26.8C17.4 21.1 22 16.4 27.7 16.4C28 16.4 28.4 16.4 28.8 16.5V14.5Z"
            fill={stroke}
          />
          <Circle cx="33.5" cy="18.5" r="2.2" fill={accent} />
          <Circle cx="36.8" cy="23.2" r="1.4" fill={accent} opacity="0.8" />
        </>
      ) : null}
    </Svg>
  );
}
