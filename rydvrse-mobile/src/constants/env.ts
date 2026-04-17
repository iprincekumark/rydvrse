export type AppVariant = "customer" | "driver" | "dual";

const appVariant = (process.env.EXPO_PUBLIC_APP_VARIANT ?? "dual") as AppVariant;

export const env = {
  apiBaseUrl: process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8084/api/v1",
  useMocks: (process.env.EXPO_PUBLIC_USE_MOCKS ?? "true") === "true",
  appVariant,
  olaMaps: {
    projectId: process.env.EXPO_PUBLIC_OLA_MAPS_PROJECT_ID ?? "",
    apiKey: process.env.EXPO_PUBLIC_OLA_MAPS_API_KEY ?? "",
    directionsUrl: process.env.EXPO_PUBLIC_OLA_MAPS_DIRECTIONS_URL ?? "https://api.olamaps.io/routing/v1/directions",
    geocodeUrl: process.env.EXPO_PUBLIC_OLA_MAPS_GEOCODE_URL ?? "https://api.olamaps.io/places/v1/geocode",
  }
};
