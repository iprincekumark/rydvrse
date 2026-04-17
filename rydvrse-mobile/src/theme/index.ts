import { Platform, TextStyle, ViewStyle } from "react-native";

// ─── Color Tokens ────────────────────────────────────────────────────
export const colors = {
  brand: {
    primary:    "#1B6EF3",
    strong:     "#1558C9",
    soft:       "#EBF2FE",
    subtle:     "#F5F8FF",
  },
  neutral: {
    900: "#1A1D21",
    700: "#3D4350",
    500: "#6B7280",
    400: "#9CA3AF",
    300: "#D1D5DB",
    200: "#E5E7EB",
    100: "#F3F4F6",
    50:  "#F9FAFB",
    0:   "#FFFFFF",
  },
  state: {
    success:     "#059669",
    successSoft: "#ECFDF5",
    warning:     "#D97706",
    warningSoft: "#FFFBEB",
    danger:      "#DC2626",
    dangerSoft:  "#FEF2F2",
    info:        "#2563EB",
    infoSoft:    "#EFF6FF",
  },
  accent: {
    primary:  "#F59E0B",
    soft:     "#FEF3C7",
  },
};

// ─── Semantic Aliases ────────────────────────────────────────────────
export const semantic = {
  text: {
    primary:   colors.neutral[900],
    secondary: colors.neutral[700],
    muted:     colors.neutral[500],
    inverted:  colors.neutral[0],
    brand:     colors.brand.primary,
  },
  bg: {
    app:      colors.neutral[0],
    surface:  colors.neutral[0],
    muted:    colors.neutral[100],
    elevated: colors.neutral[50],
    brand:    colors.brand.primary,
    brandSoft: colors.brand.soft,
  },
  border: {
    soft:   colors.neutral[200],
    strong: colors.neutral[300],
    brand:  colors.brand.primary,
  },
};

// ─── Spacing Scale ───────────────────────────────────────────────────
export const space = {
  1:  4,
  2:  8,
  3:  12,
  4:  16,
  5:  20,
  6:  24,
  7:  32,
  8:  40,
  9:  48,
  10: 64,
} as const;

// ─── Border Radius ───────────────────────────────────────────────────
export const radius = {
  none: 0,
  sm:   6,
  md:   12,
  lg:   16,
  xl:   20,
  full: 9999,
} as const;

// ─── Shadows ─────────────────────────────────────────────────────────
export const shadows = {
  sm: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.06,
      shadowRadius: 3,
      shadowOffset: { width: 0, height: 1 },
    },
    android: { elevation: 1 },
    default: {},
  }),
  md: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.08,
      shadowRadius: 12,
      shadowOffset: { width: 0, height: 4 },
    },
    android: { elevation: 3 },
    default: {},
  }),
  lg: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.10,
      shadowRadius: 24,
      shadowOffset: { width: 0, height: 8 },
    },
    android: { elevation: 5 },
    default: {},
  }),
  xl: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.12,
      shadowRadius: 40,
      shadowOffset: { width: 0, height: 16 },
    },
    android: { elevation: 8 },
    default: {},
  }),
};

// ─── Typography ──────────────────────────────────────────────────────
export const fontFamily = {
  medium:    "Manrope_500Medium",
  semiBold:  "Manrope_600SemiBold",
  bold:      "Manrope_700Bold",
  extraBold: "Manrope_800ExtraBold",
} as const;

export const textStyle = {
  displayLg: {
    fontFamily: fontFamily.extraBold,
    fontSize: 32,
    lineHeight: 38,
    letterSpacing: -0.8,
    color: semantic.text.primary,
  } satisfies TextStyle,
  displaySm: {
    fontFamily: fontFamily.extraBold,
    fontSize: 26,
    lineHeight: 32,
    letterSpacing: -0.5,
    color: semantic.text.primary,
  } satisfies TextStyle,
  headingLg: {
    fontFamily: fontFamily.bold,
    fontSize: 22,
    lineHeight: 28,
    letterSpacing: -0.3,
    color: semantic.text.primary,
  } satisfies TextStyle,
  headingSm: {
    fontFamily: fontFamily.bold,
    fontSize: 18,
    lineHeight: 24,
    letterSpacing: -0.15,
    color: semantic.text.primary,
  } satisfies TextStyle,
  bodyLg: {
    fontFamily: fontFamily.medium,
    fontSize: 16,
    lineHeight: 24,
    color: semantic.text.secondary,
  } satisfies TextStyle,
  bodyMd: {
    fontFamily: fontFamily.medium,
    fontSize: 15,
    lineHeight: 22,
    color: semantic.text.secondary,
  } satisfies TextStyle,
  bodySm: {
    fontFamily: fontFamily.medium,
    fontSize: 14,
    lineHeight: 20,
    color: semantic.text.secondary,
  } satisfies TextStyle,
  bodyStrong: {
    fontFamily: fontFamily.semiBold,
    fontSize: 15,
    lineHeight: 22,
    letterSpacing: -0.05,
    color: semantic.text.primary,
  } satisfies TextStyle,
  caption: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 16,
    color: semantic.text.muted,
  } satisfies TextStyle,
  overline: {
    fontFamily: fontFamily.semiBold,
    fontSize: 11,
    lineHeight: 16,
    letterSpacing: 0.8,
    textTransform: "uppercase",
    color: semantic.text.muted,
  } satisfies TextStyle,
  label: {
    fontFamily: fontFamily.semiBold,
    fontSize: 14,
    lineHeight: 20,
    color: semantic.text.primary,
  } satisfies TextStyle,
  tabLabel: {
    fontFamily: fontFamily.semiBold,
    fontSize: 11,
    lineHeight: 14,
  } satisfies TextStyle,
};

// ─── Animation Tokens ────────────────────────────────────────────────
export const animation = {
  fast:   150,
  normal: 250,
  slow:   400,
  spring: { damping: 20, stiffness: 180 },
};

// ─── Aggregated Theme ────────────────────────────────────────────────
export const theme = {
  colors,
  semantic,
  space,
  radius,
  shadows,
  fontFamily,
  textStyle,
  animation,
};

// ─── Backward-compatible aliases (remove after full migration) ──────
export const spacing = {
  xxs: space[1],
  xs:  space[2],
  sm:  space[3],
  md:  space[4],
  lg:  space[5],
  xl:  space[6],
  xxl: space[7],
  xxxl: space[8],
};

export const typography = {
  family: fontFamily,
  text: {
    hero:       textStyle.displayLg,
    title:      textStyle.displaySm,
    section:    textStyle.headingSm,
    overline:   textStyle.overline,
    body:       textStyle.bodyMd,
    bodyStrong: textStyle.bodyStrong,
    caption:    textStyle.caption,
  },
};
