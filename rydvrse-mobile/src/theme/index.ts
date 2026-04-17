import { Platform, TextStyle, ViewStyle } from "react-native";

export const colors = {
  primary: {
    base: "#242424",
    strong: "#111111",
    soft: "#F5F5F5"
  },
  secondary: {
    muted: "#898989",
    soft: "#F5F5F5"
  },
  background: {
    app: "#FFFFFF",
    surface: "#FFFFFF",
    muted: "#F5F5F5",
    elevated: "#FAFAFA"
  },
  text: {
    primary: "#242424",
    secondary: "#898989",
    muted: "#A3A3A3",
    inverted: "#FFFFFF"
  },
  border: {
    soft: "rgba(34,42,53,0.10)",
    strong: "rgba(34,42,53,0.18)"
  },
  state: {
    success: "#167447",
    successSoft: "#F2F8F4",
    warning: "#8C5A12",
    warningSoft: "#FAF7F1",
    danger: "#C43C3C",
    dangerSoft: "#FBF2F2",
    info: "#0099FF",
    infoSoft: "#F3F8FC"
  }
};

export const spacing = {
  xxs: 4,
  xs: 8,
  sm: 12,
  md: 16,
  lg: 20,
  xl: 24,
  xxl: 32,
  xxxl: 40
};

export const radius = {
  sm: 4,
  md: 8,
  lg: 12,
  xl: 16,
  pill: 9999
};

export const shadows = {
  card: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#222A35",
      shadowOpacity: 0.08,
      shadowRadius: 8,
      shadowOffset: { width: 0, height: 4 }
    },
    android: {
      elevation: 2
    },
    default: {}
  }),
  heavy: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#131316",
      shadowOpacity: 0.1,
      shadowRadius: 14,
      shadowOffset: { width: 0, height: 8 }
    },
    android: {
      elevation: 4
    },
    default: {}
  })
};

export const typography = {
  family: {
    displaySemiBold: "Manrope_700Bold",
    displayBold: "Manrope_800ExtraBold",
    medium: "Manrope_500Medium",
    semiBold: "Manrope_600SemiBold",
    bold: "Manrope_700Bold",
    extraBold: "Manrope_800ExtraBold"
  },
  text: {
    hero: {
      fontFamily: "Manrope_800ExtraBold",
      fontSize: 42,
      lineHeight: 46,
      letterSpacing: -1.1,
      color: colors.text.primary
    } satisfies TextStyle,
    title: {
      fontFamily: "Manrope_800ExtraBold",
      fontSize: 24,
      lineHeight: 29,
      letterSpacing: -0.5,
      color: colors.text.primary
    } satisfies TextStyle,
    section: {
      fontFamily: "Manrope_700Bold",
      fontSize: 18,
      lineHeight: 22,
      letterSpacing: -0.15,
      color: colors.text.primary
    } satisfies TextStyle,
    overline: {
      fontFamily: "Manrope_600SemiBold",
      fontSize: 11,
      lineHeight: 16,
      color: colors.text.secondary,
      letterSpacing: 0.9,
      textTransform: "uppercase"
    } satisfies TextStyle,
    body: {
      fontFamily: "Manrope_500Medium",
      fontSize: 15,
      lineHeight: 22,
      color: colors.text.secondary
    } satisfies TextStyle,
    bodyStrong: {
      fontFamily: "Manrope_600SemiBold",
      fontSize: 15,
      lineHeight: 22,
      letterSpacing: -0.05,
      color: colors.text.primary
    } satisfies TextStyle,
    caption: {
      fontFamily: "Manrope_500Medium",
      fontSize: 12,
      lineHeight: 18,
      color: colors.text.muted
    } satisfies TextStyle
  }
};

export const theme = {
  colors,
  spacing,
  radius,
  shadows,
  typography
};
