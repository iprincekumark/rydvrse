import { CustomerServiceType } from "@/constants/serviceTypes";
import { env } from "@/constants/env";

type Coordinate = {
  latitude: number;
  longitude: number;
};

export type RouteEstimate = {
  roundedDistanceKm: number;
  predictedDriveMinutes: number;
  driverPickupDistanceKm: number;
  driverPickupEtaMinutes: number;
  source: "ola_maps" | "fallback";
};

type CalculateRouteEstimateInput = {
  pickupLabel: string;
  dropLabel: string;
  serviceType: CustomerServiceType;
  pickupCoords?: Coordinate | null;
  dropCoords?: Coordinate | null;
};

const KNOWN_BENGALURU_POINTS: Array<{ tokens: string[]; coordinate: Coordinate }> = [
  { tokens: ["koramangala"], coordinate: { latitude: 12.9352, longitude: 77.6245 } },
  { tokens: ["whitefield"], coordinate: { latitude: 12.9698, longitude: 77.75 } },
  { tokens: ["indiranagar"], coordinate: { latitude: 12.9784, longitude: 77.6408 } },
  { tokens: ["lavelle"], coordinate: { latitude: 12.9719, longitude: 77.5962 } },
  { tokens: ["current location"], coordinate: { latitude: 12.9352, longitude: 77.6245 } },
  { tokens: ["bengaluru", "bangalore"], coordinate: { latitude: 12.9716, longitude: 77.5946 } },
];

const KNOWN_ROUTE_OVERRIDES: Array<{
  from: string;
  to: string;
  distanceKm: number;
  minutes: number;
}> = [
  { from: "koramangala", to: "whitefield", distanceKm: 31, minutes: 105 },
  { from: "whitefield", to: "koramangala", distanceKm: 31, minutes: 105 },
];

export async function calculateRouteEstimate({
  pickupLabel,
  dropLabel,
  serviceType,
  pickupCoords,
  dropCoords,
}: CalculateRouteEstimateInput): Promise<RouteEstimate> {
  const knownRoute =
    pickupCoords && dropCoords
      ? null
      : getKnownRouteEstimate(pickupLabel, dropLabel, serviceType);
  if (knownRoute) {
    return knownRoute;
  }

  const pickup = pickupCoords ?? (await resolveCoordinate(pickupLabel));
  const drop = dropCoords ?? (await resolveCoordinate(dropLabel));
  const olaEstimate = pickup && drop ? await fetchOlaMapsEstimate(pickup, drop, serviceType) : null;

  if (olaEstimate) {
    return olaEstimate;
  }

  return fallbackEstimate(pickup, drop, serviceType);
}

function getKnownRouteEstimate(
  pickupLabel: string,
  dropLabel: string,
  serviceType: CustomerServiceType,
): RouteEstimate | null {
  const pickup = normalize(pickupLabel);
  const drop = normalize(dropLabel);
  const override = KNOWN_ROUTE_OVERRIDES.find((route) => pickup.includes(route.from) && drop.includes(route.to));

  if (!override) {
    return null;
  }

  return buildEstimate(override.distanceKm, override.minutes, serviceType, "fallback");
}

async function resolveCoordinate(label: string): Promise<Coordinate | null> {
  const local = resolveLocalCoordinate(label);
  if (local) {
    return local;
  }

  return geocodeWithOlaMaps(label);
}

function resolveLocalCoordinate(label: string): Coordinate | null {
  const normalized = normalize(label);
  const match = KNOWN_BENGALURU_POINTS.find((point) => point.tokens.some((token) => normalized.includes(token)));
  return match?.coordinate ?? null;
}

async function geocodeWithOlaMaps(label: string): Promise<Coordinate | null> {
  if (!env.olaMaps.apiKey || !label.trim()) {
    return null;
  }

  try {
    const url = new URL(env.olaMaps.geocodeUrl);
    url.searchParams.set("address", `${label}, Bengaluru`);
    url.searchParams.set("api_key", env.olaMaps.apiKey);

    const response = await fetch(url.toString(), {
      headers: buildOlaHeaders(),
    });

    if (!response.ok) {
      return null;
    }

    const data = await response.json();
    return extractCoordinate(data);
  } catch {
    return null;
  }
}

async function fetchOlaMapsEstimate(
  pickup: Coordinate,
  drop: Coordinate,
  serviceType: CustomerServiceType,
): Promise<RouteEstimate | null> {
  if (!env.olaMaps.apiKey) {
    return null;
  }

  try {
    const url = new URL(env.olaMaps.directionsUrl);
    url.searchParams.set("origin", `${pickup.latitude},${pickup.longitude}`);
    url.searchParams.set("destination", `${drop.latitude},${drop.longitude}`);
    url.searchParams.set("mode", "driving");
    url.searchParams.set("alternatives", "false");
    url.searchParams.set("overview", "simplified");
    url.searchParams.set("api_key", env.olaMaps.apiKey);

    const response = await fetch(url.toString(), {
      headers: buildOlaHeaders(),
    });

    if (!response.ok) {
      return null;
    }

    const data = await response.json();
    const distanceMeters = extractDistanceMeters(data);
    const durationSeconds = extractDurationSeconds(data);

    if (!distanceMeters || !durationSeconds) {
      return null;
    }

    return buildEstimate(Math.ceil(distanceMeters / 1000), Math.ceil(durationSeconds / 60), serviceType, "ola_maps");
  } catch {
    return null;
  }
}

function buildOlaHeaders() {
  return {
    Accept: "application/json",
    "X-API-Key": env.olaMaps.apiKey,
    "X-Project-ID": env.olaMaps.projectId,
  };
}

function fallbackEstimate(
  pickup: Coordinate | null,
  drop: Coordinate | null,
  serviceType: CustomerServiceType,
): RouteEstimate {
  if (!pickup || !drop) {
    return buildEstimate(31, 105, serviceType, "fallback");
  }

  const straightLineKm = haversineKm(pickup, drop);
  const roadDistanceKm = Math.max(8, Math.ceil(straightLineKm * 1.45));
  const minutes = Math.max(35, Math.ceil((roadDistanceKm / 18) * 60));
  return buildEstimate(roadDistanceKm, minutes, serviceType, "fallback");
}

function buildEstimate(
  oneWayDistanceKm: number,
  oneWayMinutes: number,
  serviceType: CustomerServiceType,
  source: RouteEstimate["source"],
): RouteEstimate {
  const isRoundTrip = serviceType === "ROUND_TRIP";
  const roundedDistanceKm = isRoundTrip ? Math.ceil(oneWayDistanceKm * 2) : Math.ceil(oneWayDistanceKm);
  const predictedDriveMinutes = isRoundTrip ? Math.ceil(oneWayMinutes * 2 + 30) : Math.ceil(oneWayMinutes);
  const driverPickupDistanceKm = Math.min(10, Math.max(4, Math.ceil(oneWayDistanceKm * 0.22)));
  const driverPickupEtaMinutes = Math.min(30, Math.max(12, Math.ceil(driverPickupDistanceKm * 3.2)));

  return {
    roundedDistanceKm,
    predictedDriveMinutes,
    driverPickupDistanceKm,
    driverPickupEtaMinutes,
    source,
  };
}

function extractCoordinate(data: any): Coordinate | null {
  const candidates = [
    data?.geocodingResults?.[0]?.geometry?.location,
    data?.predictions?.[0]?.geometry?.location,
    data?.results?.[0]?.geometry?.location,
    data?.data?.[0]?.geometry?.location,
    data?.data?.results?.[0]?.geometry?.location,
  ];

  for (const candidate of candidates) {
    const latitude = Number(candidate?.lat ?? candidate?.latitude);
    const longitude = Number(candidate?.lng ?? candidate?.longitude);
    if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
      return { latitude, longitude };
    }
  }

  return null;
}

function extractDistanceMeters(data: any): number | null {
  const route = data?.routes?.[0] ?? data?.data?.routes?.[0];
  const leg = route?.legs?.[0];
  return firstFiniteNumber([
    leg?.distance?.value,
    leg?.distance,
    route?.distance,
    route?.summary?.distance,
    data?.distance,
  ]);
}

function extractDurationSeconds(data: any): number | null {
  const route = data?.routes?.[0] ?? data?.data?.routes?.[0];
  const leg = route?.legs?.[0];
  return firstFiniteNumber([
    leg?.duration_in_traffic?.value,
    leg?.duration?.value,
    leg?.duration,
    route?.duration,
    route?.summary?.duration,
    data?.duration,
  ]);
}

function firstFiniteNumber(values: unknown[]) {
  for (const value of values) {
    const numberValue = Number(value);
    if (Number.isFinite(numberValue) && numberValue > 0) {
      return numberValue;
    }
  }

  return null;
}

function haversineKm(start: Coordinate, end: Coordinate) {
  const earthRadiusKm = 6371;
  const deltaLatitude = toRadians(end.latitude - start.latitude);
  const deltaLongitude = toRadians(end.longitude - start.longitude);
  const startLatitude = toRadians(start.latitude);
  const endLatitude = toRadians(end.latitude);
  const a =
    Math.sin(deltaLatitude / 2) * Math.sin(deltaLatitude / 2) +
    Math.cos(startLatitude) * Math.cos(endLatitude) * Math.sin(deltaLongitude / 2) * Math.sin(deltaLongitude / 2);
  return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function toRadians(value: number) {
  return (value * Math.PI) / 180;
}

function normalize(value: string) {
  return value.trim().toLowerCase();
}
