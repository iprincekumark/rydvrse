import { Platform, TextStyle, ViewStyle } from "react-native";

export const colors = {
  primary: {
    base: "#0C6D69",
    strong: "#084E4B",
    soft: "#D7F2EE"
  },
  secondary: {
    amber: "#C8842F",
    amberSoft: "#FAEDD8"
  },
  background: {
    app: "#F6F4EE",
    surface: "#FFFFFF",
    muted: "#EDF1ED",
    elevated: "#FBFAF7"
  },
  text: {
    primary: "#163130",
    secondary: "#577271",
    muted: "#7F9694",
    inverted: "#FFFFFF"
  },
  border: {
    soft: "#D7E1DD",
    strong: "#B7C7C3"
  },
  state: {
    success: "#1E8E5C",
    successSoft: "#E0F2E8",
    warning: "#B36A09",
    warningSoft: "#FFF2DE",
    danger: "#C43C3C",
    dangerSoft: "#FCE8E8",
    info: "#2A6AC7",
    infoSoft: "#E6F0FF"
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
  sm: 10,
  md: 16,
  lg: 22,
  xl: 30,
  pill: 999
};

export const shadows = {
  card: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#103533",
      shadowOpacity: 0.1,
      shadowRadius: 16,
      shadowOffset: { width: 0, height: 8 }
    },
    android: {
      elevation: 4
    },
    default: {}
  }),
  heavy: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#103533",
      shadowOpacity: 0.16,
      shadowRadius: 24,
      shadowOffset: { width: 0, height: 12 }
    },
    android: {
      elevation: 8
    },
    default: {}
  })
};

export const typography = {
  family: {
    displaySemiBold: "CormorantGaramond_600SemiBold",
    displayBold: "CormorantGaramond_700Bold",
    medium: "Manrope_500Medium",
    semiBold: "Manrope_600SemiBold",
    bold: "Manrope_700Bold",
    extraBold: "Manrope_800ExtraBold"
  },
  text: {
    hero: {
      fontFamily: "CormorantGaramond_700Bold",
      fontSize: 40,
      lineHeight: 42,
      letterSpacing: 0.2,
      color: colors.text.primary
    } satisfies TextStyle,
    title: {
      fontFamily: "Manrope_700Bold",
      fontSize: 22,
      lineHeight: 28,
      color: colors.text.primary
    } satisfies TextStyle,
    section: {
      fontFamily: "Manrope_700Bold",
      fontSize: 18,
      lineHeight: 24,
      color: colors.text.primary
    } satisfies TextStyle,
    overline: {
      fontFamily: "Manrope_600SemiBold",
      fontSize: 11,
      lineHeight: 16,
      color: colors.primary.base,
      letterSpacing: 1.1,
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
