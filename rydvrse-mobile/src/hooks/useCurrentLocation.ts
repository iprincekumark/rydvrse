import { useCallback, useEffect, useRef, useState } from "react";

/**
 * Minimal lat/lng pair. Matches what our map and routing services expect.
 */
export type Coordinates = {
  latitude: number;
  longitude: number;
};

/**
 * Bengaluru city center — used as a safe fallback when device GPS is
 * unavailable (emulator without location, denied permission, Expo Go without
 * the expo-location module installed yet, etc.).
 */
export const BENGALURU_FALLBACK: Coordinates = {
  latitude: 12.9716,
  longitude: 77.5946,
};

export type LocationStatus = "idle" | "requesting" | "granted" | "denied" | "unavailable";

export type UseCurrentLocationResult = {
  coords: Coordinates;
  status: LocationStatus;
  usingFallback: boolean;
  error: string | null;
  refresh: () => Promise<void>;
};

/**
 * Dynamically load `expo-location` if it is installed. Wrapped in a try/catch
 * so the app does not crash when running on an environment where the module
 * has not been installed yet (Expo Go with an older prebuild, for example).
 */
function loadExpoLocation(): any | null {
  try {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    return require("expo-location");
  } catch {
    return null;
  }
}

/**
 * `useCurrentLocation` returns the user's current coordinates.
 *
 * Behaviour:
 *  - Attempts to request foreground permission and read the last known
 *    position from `expo-location` when the module is available.
 *  - Falls back to a Bengaluru default while the request is in flight, or if
 *    the module/permission is not available. The `usingFallback` flag tells
 *    the caller whether the coordinates are a real device fix.
 *  - Exposes `refresh()` so UI "recenter" buttons can re-request the fix.
 */
export function useCurrentLocation(
  options: { autoRequest?: boolean; liveUpdates?: boolean } = {},
): UseCurrentLocationResult {
  const { autoRequest = true, liveUpdates = false } = options;
  const [coords, setCoords] = useState<Coordinates>(BENGALURU_FALLBACK);
  const [status, setStatus] = useState<LocationStatus>("idle");
  const [usingFallback, setUsingFallback] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const mountedRef = useRef(true);
  const watcherRef = useRef<{ remove: () => void } | null>(null);

  const request = useCallback(async () => {
    const Location = loadExpoLocation();
    if (!Location) {
      if (mountedRef.current) {
        setStatus("unavailable");
        setUsingFallback(true);
      }
      return;
    }

    try {
      if (mountedRef.current) {
        setStatus("requesting");
      }

      const permission = await Location.requestForegroundPermissionsAsync();
      if (!mountedRef.current) {
        return;
      }

      if (permission.status !== "granted") {
        setStatus("denied");
        setUsingFallback(true);
        return;
      }

      const position = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy?.Balanced ?? 3,
      });
      if (!mountedRef.current) {
        return;
      }

      setCoords({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      });
      setStatus("granted");
      setUsingFallback(false);
      setError(null);

      if (liveUpdates && !watcherRef.current && typeof Location.watchPositionAsync === "function") {
        watcherRef.current = await Location.watchPositionAsync(
          {
            accuracy: Location.Accuracy?.Balanced ?? 3,
            timeInterval: 5000,
            distanceInterval: 20,
          },
          (next: { coords?: { latitude?: number; longitude?: number } }) => {
            if (!mountedRef.current) {
              return;
            }
            const latitude = Number(next.coords?.latitude);
            const longitude = Number(next.coords?.longitude);
            if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
              setCoords({ latitude, longitude });
              setStatus("granted");
              setUsingFallback(false);
            }
          },
        );
      }
    } catch (gpsError) {
      if (!mountedRef.current) {
        return;
      }
      setError(
        gpsError instanceof Error ? gpsError.message : "Unable to read your location."
      );
      setStatus("unavailable");
      setUsingFallback(true);
    }
  }, [liveUpdates]);

  useEffect(() => {
    mountedRef.current = true;

    if (autoRequest) {
      void request();
    }

    return () => {
      mountedRef.current = false;
      watcherRef.current?.remove();
      watcherRef.current = null;
    };
  }, [autoRequest, request]);

  return {
    coords,
    status,
    usingFallback,
    error,
    refresh: request,
  };
}
