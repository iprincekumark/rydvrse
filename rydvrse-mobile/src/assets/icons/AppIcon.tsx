import React from "react";
import Svg, { Circle, Path, Rect } from "react-native-svg";

export type AppIconName =
  | "home"
  | "calendar"
  | "help"
  | "profile"
  | "jobs"
  | "wallet"
  | "clock"
  | "pin"
  | "route"
  | "ticket"
  | "driver"
  | "car"
  | "shield"
  | "check"
  | "alert"
  | "star"
  | "phone"
  | "document"
  | "bank"
  | "camera"
  | "city"
  | "logout"
  | "spark"
  | "language"
  | "status"
  | "eta"
  | "earnings"
  | "id"
  | "arrowRight"
  | "arrowLeft";

type AppIconProps = {
  name: AppIconName;
  size?: number;
  color?: string;
  secondaryColor?: string;
};

export function AppIcon({
  name,
  size = 20,
  color = "#1A1D21",
  secondaryColor = "#9CA3AF"
}: AppIconProps) {
  const strokeProps = {
    stroke: color,
    strokeWidth: 2,
    strokeLinecap: "round" as const,
    strokeLinejoin: "round" as const
  };

  return (
    <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
      {name === "home" ? (
        <>
          <Path d="M4 10.5L12 4L20 10.5" {...strokeProps} />
          <Path d="M6.5 9.5V19H17.5V9.5" {...strokeProps} />
        </>
      ) : null}
      {name === "calendar" ? (
        <>
          <Rect x="4" y="6" width="16" height="14" rx="3" {...strokeProps} />
          <Path d="M8 4V8" {...strokeProps} />
          <Path d="M16 4V8" {...strokeProps} />
          <Path d="M4 10H20" {...strokeProps} />
        </>
      ) : null}
      {name === "help" ? (
        <>
          <Circle cx="12" cy="12" r="8" {...strokeProps} />
          <Path d="M9.8 9.4C10.2 8.2 11.2 7.5 12.4 7.5C13.9 7.5 15 8.4 15 9.8C15 11 14.3 11.6 13.2 12.3C12.4 12.8 12 13.3 12 14.4" {...strokeProps} />
          <Circle cx="12" cy="17.4" r="0.8" fill={secondaryColor} />
        </>
      ) : null}
      {name === "profile" ? (
        <>
          <Circle cx="12" cy="9" r="3.2" {...strokeProps} />
          <Path d="M6.5 18C7.8 15.9 9.7 15 12 15C14.3 15 16.2 15.9 17.5 18" {...strokeProps} />
        </>
      ) : null}
      {name === "jobs" ? (
        <>
          <Rect x="4.5" y="7" width="15" height="11.5" rx="2.8" {...strokeProps} />
          <Path d="M9 7V5.5H15V7" {...strokeProps} />
          <Path d="M4.5 11H19.5" {...strokeProps} />
        </>
      ) : null}
      {name === "wallet" ? (
        <>
          <Rect x="4" y="7" width="16" height="11" rx="3" {...strokeProps} />
          <Path d="M15 12H20" {...strokeProps} />
          <Circle cx="15.5" cy="12" r="1" fill={secondaryColor} />
        </>
      ) : null}
      {name === "clock" ? (
        <>
          <Circle cx="12" cy="12" r="8" {...strokeProps} />
          <Path d="M12 8V12L15 14" {...strokeProps} />
        </>
      ) : null}
      {name === "pin" ? (
        <>
          <Path d="M12 20C15.8 15.8 18 12.6 18 9.5C18 6.4 15.3 4 12 4C8.7 4 6 6.4 6 9.5C6 12.6 8.2 15.8 12 20Z" {...strokeProps} />
          <Circle cx="12" cy="9.5" r="2.2" fill={secondaryColor} />
        </>
      ) : null}
      {name === "route" ? (
        <>
          <Circle cx="6" cy="17" r="2.3" fill={secondaryColor} />
          <Circle cx="18" cy="7" r="2.3" fill={color} />
          <Path d="M7.8 16C11 11.2 13.3 9.6 16.2 8.3" {...strokeProps} />
        </>
      ) : null}
      {name === "ticket" ? (
        <>
          <Path d="M6 7.5H18C18 8.6 18.9 9.5 20 9.5V14.5C18.9 14.5 18 15.4 18 16.5H6C6 15.4 5.1 14.5 4 14.5V9.5C5.1 9.5 6 8.6 6 7.5Z" {...strokeProps} />
          <Path d="M12 7.5V16.5" stroke={secondaryColor} strokeWidth="2" strokeDasharray="2 2" />
        </>
      ) : null}
      {name === "driver" ? (
        <>
          <Circle cx="9" cy="9" r="2.8" {...strokeProps} />
          <Path d="M4.8 17.3C5.7 15.6 7.1 14.8 9 14.8C10.9 14.8 12.3 15.6 13.2 17.3" {...strokeProps} />
          <Rect x="14.5" y="7.5" width="4.5" height="9" rx="1.8" {...strokeProps} />
        </>
      ) : null}
      {name === "car" ? (
        <>
          <Path d="M5 14L6.8 9.8C7.1 9.1 7.8 8.6 8.6 8.6H15.4C16.2 8.6 16.9 9.1 17.2 9.8L19 14" {...strokeProps} />
          <Path d="M5 14V16.8C5 17.5 5.5 18 6.2 18H17.8C18.5 18 19 17.5 19 16.8V14" {...strokeProps} />
          <Circle cx="7.8" cy="15.2" r="1.1" fill={secondaryColor} />
          <Circle cx="16.2" cy="15.2" r="1.1" fill={secondaryColor} />
        </>
      ) : null}
      {name === "shield" ? (
        <>
          <Path d="M12 4.5L18 7V11.2C18 15 15.5 17.8 12 19.5C8.5 17.8 6 15 6 11.2V7L12 4.5Z" {...strokeProps} />
          <Path d="M9.5 11.9L11.2 13.6L14.8 10" {...strokeProps} />
        </>
      ) : null}
      {name === "check" ? (
        <Path d="M5 12.5L9.4 16.7L19 7.5" {...strokeProps} />
      ) : null}
      {name === "alert" ? (
        <>
          <Path d="M12 5L19 18H5L12 5Z" {...strokeProps} />
          <Path d="M12 9V12.8" {...strokeProps} />
          <Circle cx="12" cy="15.7" r="0.9" fill={secondaryColor} />
        </>
      ) : null}
      {name === "star" ? (
        <Path d="M12 5.5L13.9 9.5L18.2 10.1L15.1 13.2L15.8 17.5L12 15.4L8.2 17.5L8.9 13.2L5.8 10.1L10.1 9.5L12 5.5Z" {...strokeProps} />
      ) : null}
      {name === "phone" ? (
        <>
          <Path d="M8.5 5.5L10.5 8.7C10.8 9.2 10.8 9.8 10.4 10.2L9.5 11.1C10.2 12.7 11.3 13.8 12.9 14.5L13.8 13.6C14.2 13.2 14.8 13.2 15.3 13.5L18.5 15.5C19.1 15.9 19.3 16.7 18.9 17.3L17.9 18.8C17.5 19.4 16.8 19.7 16.1 19.5C10 17.8 6.2 14 4.5 7.9C4.3 7.2 4.6 6.5 5.2 6.1L6.7 5.1C7.3 4.7 8.1 4.9 8.5 5.5Z" {...strokeProps} />
        </>
      ) : null}
      {name === "document" ? (
        <>
          <Path d="M8 4.5H14.5L18 8V19.5H8C6.9 19.5 6 18.6 6 17.5V6.5C6 5.4 6.9 4.5 8 4.5Z" {...strokeProps} />
          <Path d="M14 4.5V8.5H18" {...strokeProps} />
          <Path d="M9 12H15" {...strokeProps} />
          <Path d="M9 15H13.5" {...strokeProps} />
        </>
      ) : null}
      {name === "bank" ? (
        <>
          <Path d="M4.5 9L12 5L19.5 9" {...strokeProps} />
          <Path d="M6.5 9.5V16.5" {...strokeProps} />
          <Path d="M10.5 9.5V16.5" {...strokeProps} />
          <Path d="M13.5 9.5V16.5" {...strokeProps} />
          <Path d="M17.5 9.5V16.5" {...strokeProps} />
          <Path d="M4 18.5H20" {...strokeProps} />
        </>
      ) : null}
      {name === "camera" ? (
        <>
          <Rect x="4.5" y="8" width="15" height="10.5" rx="2.5" {...strokeProps} />
          <Path d="M8 8L9.2 6.3H14.8L16 8" {...strokeProps} />
          <Circle cx="12" cy="13.2" r="2.6" {...strokeProps} />
        </>
      ) : null}
      {name === "city" ? (
        <>
          <Rect x="5" y="8.5" width="5" height="10.5" {...strokeProps} />
          <Rect x="10.5" y="5.5" width="4.5" height="13.5" {...strokeProps} />
          <Rect x="15.5" y="10.5" width="3.5" height="8.5" {...strokeProps} />
        </>
      ) : null}
      {name === "logout" ? (
        <>
          <Path d="M10 6H7.5C6.7 6 6 6.7 6 7.5V16.5C6 17.3 6.7 18 7.5 18H10" {...strokeProps} />
          <Path d="M13 9L17 12L13 15" {...strokeProps} />
          <Path d="M17 12H10" {...strokeProps} />
        </>
      ) : null}
      {name === "spark" ? (
        <>
          <Path d="M12 4.8L13.6 9.2L18 10.8L13.6 12.4L12 16.8L10.4 12.4L6 10.8L10.4 9.2L12 4.8Z" {...strokeProps} />
          <Path d="M18.5 4.5L19 6L20.5 6.5L19 7L18.5 8.5L18 7L16.5 6.5L18 6L18.5 4.5Z" fill={secondaryColor} />
        </>
      ) : null}
      {name === "language" ? (
        <>
          <Path d="M5.5 8.5H14.5" {...strokeProps} />
          <Path d="M10 6V8.5C10 11.8 8.6 14.8 6 16.8" {...strokeProps} />
          <Path d="M8.2 13C9.8 14.7 11.8 16 14.2 16.8" {...strokeProps} />
          <Path d="M16.5 8.5L19.5 17" {...strokeProps} />
          <Path d="M15.3 14H20.7" {...strokeProps} />
        </>
      ) : null}
      {name === "status" ? (
        <>
          <Circle cx="7" cy="12" r="1.8" fill={secondaryColor} />
          <Path d="M11 8H18" {...strokeProps} />
          <Path d="M11 12H18" {...strokeProps} />
          <Path d="M11 16H16" {...strokeProps} />
        </>
      ) : null}
      {name === "eta" ? (
        <>
          <Circle cx="12" cy="12" r="8" {...strokeProps} />
          <Path d="M12 12L15.5 9" {...strokeProps} />
          <Circle cx="12" cy="12" r="1" fill={secondaryColor} />
        </>
      ) : null}
      {name === "earnings" ? (
        <>
          <Path d="M7 16L10 12.5L12.8 14.6L17 9" {...strokeProps} />
          <Path d="M17 9H14.5" {...strokeProps} />
          <Path d="M17 9V11.5" {...strokeProps} />
        </>
      ) : null}
      {name === "id" ? (
        <>
          <Rect x="4.5" y="6.5" width="15" height="11" rx="2.5" {...strokeProps} />
          <Circle cx="9" cy="11" r="1.8" {...strokeProps} />
          <Path d="M7 15C7.5 13.8 8.1 13.2 9 13.2C9.9 13.2 10.5 13.8 11 15" {...strokeProps} />
          <Path d="M13 10H17" {...strokeProps} />
          <Path d="M13 13H16" {...strokeProps} />
        </>
      ) : null}
      {name === "arrowRight" ? (
        <Path d="M6 12H18M18 12L13.8 7.8M18 12L13.8 16.2" {...strokeProps} />
      ) : null}
      {name === "arrowLeft" ? (
        <Path d="M18 12H6M6 12L10.2 7.8M6 12L10.2 16.2" {...strokeProps} />
      ) : null}
    </Svg>
  );
}
