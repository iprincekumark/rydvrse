import { env } from "@/constants/env";

/**
 * A normalised place suggestion used across the app.
 *
 * Shape is intentionally loose so we can accept either:
 *  - Ola Maps responses
 *  - Local "recent places" cached in Redux
 *  - Manual entries (user typed a label, we do not yet have coordinates)
 */
export type PlaceSuggestion = {
  id: string;
  label: string;
  description?: string;
  latitude?: number;
  longitude?: number;
  source: "ola" | "recent" | "manual";
};

const REQUEST_TIMEOUT_MS = 8000;

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T | null> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      ...init,
      signal: controller.signal,
      headers: {
        "Content-Type": "application/json",
        ...(init?.headers ?? {}),
      },
    });

    if (!response.ok) {
      return null;
    }

    return (await response.json()) as T;
  } catch {
    return null;
  } finally {
    clearTimeout(timeout);
  }
}

function olaHeaders(): Record<string, string> {
  const headers: Record<string, string> = {};
  if (env.olaMaps.apiKey) {
    headers["X-API-Key"] = env.olaMaps.apiKey;
    headers["Authorization"] = `Bearer ${env.olaMaps.apiKey}`;
  }
  if (env.olaMaps.projectId) {
    headers["X-Project-ID"] = env.olaMaps.projectId;
    headers["X-Request-Id"] = `rydvrse-${Date.now()}`;
  }
  return headers;
}

/**
 * Query a static list of curated Bengaluru anchors when the network call
 * fails. Keeps the autocomplete usable even when Ola Maps is unreachable,
 * which matters because the UI uses autocomplete as the primary way to
 * select pickup and drop locations.
 */
const FALLBACK_SUGGESTIONS: PlaceSuggestion[] = [
  {
    id: "fallback-koramangala",
    label: "Koramangala 4th Block",
    description: "Bengaluru, Karnataka",
    latitude: 12.9352,
    longitude: 77.6245,
    source: "manual",
  },
  {
    id: "fallback-indiranagar",
    label: "Indiranagar 100 Feet Road",
    description: "Bengaluru, Karnataka",
    latitude: 12.9784,
    longitude: 77.6408,
    source: "manual",
  },
  {
    id: "fallback-whitefield",
    label: "Whitefield Main Road",
    description: "Bengaluru, Karnataka",
    latitude: 12.9698,
    longitude: 77.75,
    source: "manual",
  },
  {
    id: "fallback-hsr",
    label: "HSR Layout Sector 1",
    description: "Bengaluru, Karnataka",
    latitude: 12.9121,
    longitude: 77.6446,
    source: "manual",
  },
  {
    id: "fallback-electronic-city",
    label: "Electronic City Phase 1",
    description: "Bengaluru, Karnataka",
    latitude: 12.8452,
    longitude: 77.6602,
    source: "manual",
  },
  {
    id: "fallback-mg-road",
    label: "MG Road Metro Station",
    description: "Bengaluru, Karnataka",
    latitude: 12.9756,
    longitude: 77.6064,
    source: "manual",
  },
  {
    id: "fallback-btm",
    label: "BTM Layout 2nd Stage",
    description: "Bengaluru, Karnataka",
    latitude: 12.9165,
    longitude: 77.6101,
    source: "manual",
  },
  {
    id: "fallback-jayanagar",
    label: "Jayanagar 4th Block",
    description: "Bengaluru, Karnataka",
    latitude: 12.9299,
    longitude: 77.5833,
    source: "manual",
  },
];

function filterFallback(query: string): PlaceSuggestion[] {
  const normalised = query.trim().toLowerCase();
  if (!normalised) {
    return FALLBACK_SUGGESTIONS.slice(0, 6);
  }
  return FALLBACK_SUGGESTIONS.filter((item) =>
    `${item.label} ${item.description ?? ""}`.toLowerCase().includes(normalised)
  ).slice(0, 6);
}

type OlaPrediction = {
  place_id?: string;
  description?: string;
  structured_formatting?: {
    main_text?: string;
    secondary_text?: string;
  };
  geometry?: {
    location?: {
      lat?: number;
      lng?: number;
    };
  };
  lat?: number;
  lng?: number;
};

type OlaAutocompleteResponse = {
  predictions?: OlaPrediction[];
  results?: OlaPrediction[];
  status?: string;
};

function normalisePrediction(prediction: OlaPrediction, index: number): PlaceSuggestion {
  const main = prediction.structured_formatting?.main_text ?? prediction.description ?? "";
  const secondary = prediction.structured_formatting?.secondary_text ?? "";
  const lat =
    prediction.geometry?.location?.lat ?? prediction.lat ?? undefined;
  const lng =
    prediction.geometry?.location?.lng ?? prediction.lng ?? undefined;
  return {
    id: prediction.place_id ?? `ola-${index}`,
    label: main || "Suggested place",
    description: secondary || prediction.description,
    latitude: typeof lat === "number" ? lat : undefined,
    longitude: typeof lng === "number" ? lng : undefined,
    source: "ola",
  };
}

/**
 * Autocomplete a free-form query using the Ola Maps Places API. Falls back to
 * a curated list of Bengaluru landmarks when:
 *  - no API key is configured (dev without .env.local)
 *  - the network request fails or times out
 *  - Ola Maps returns an empty payload
 */
export async function autocompletePlaces(
  query: string,
  options: { biasCoords?: { latitude: number; longitude: number } } = {}
): Promise<PlaceSuggestion[]> {
  const trimmed = query.trim();

  if (!env.olaMaps.apiKey || trimmed.length < 2) {
    return filterFallback(trimmed);
  }

  const params = new URLSearchParams();
  params.set("input", trimmed);
  params.set("api_key", env.olaMaps.apiKey);
  if (options.biasCoords) {
    params.set(
      "location",
      `${options.biasCoords.latitude},${options.biasCoords.longitude}`
    );
    params.set("radius", "25000");
  }

  const response = await fetchJson<OlaAutocompleteResponse>(
    `${env.olaMaps.autocompleteUrl}?${params.toString()}`,
    { method: "GET", headers: olaHeaders() }
  );

  const predictions = response?.predictions ?? response?.results ?? [];
  if (!predictions.length) {
    return filterFallback(trimmed);
  }

  return predictions.map((prediction, index) => normalisePrediction(prediction, index));
}

type OlaReverseGeocodeResponse = {
  results?: Array<{
    formatted_address?: string;
    name?: string;
    place_id?: string;
    geometry?: { location?: { lat?: number; lng?: number } };
  }>;
};

/**
 * Reverse-geocode a coordinate pair using Ola Maps. Returns a human-readable
 * label the UI can display ("Koramangala 4th Block") alongside the resolved
 * coordinates. Falls back to a short lat/lng string when the API is
 * unreachable — never throws.
 */
export async function reverseGeocode(
  coords: { latitude: number; longitude: number }
): Promise<PlaceSuggestion> {
  const fallback: PlaceSuggestion = {
    id: `reverse-${coords.latitude.toFixed(4)}-${coords.longitude.toFixed(4)}`,
    label: `Current location • ${coords.latitude.toFixed(3)}, ${coords.longitude.toFixed(3)}`,
    description: "Bengaluru, Karnataka",
    latitude: coords.latitude,
    longitude: coords.longitude,
    source: "manual",
  };

  if (!env.olaMaps.apiKey) {
    return fallback;
  }

  const params = new URLSearchParams();
  params.set("latlng", `${coords.latitude},${coords.longitude}`);
  params.set("api_key", env.olaMaps.apiKey);

  const response = await fetchJson<OlaReverseGeocodeResponse>(
    `${env.olaMaps.reverseGeocodeUrl}?${params.toString()}`,
    { method: "GET", headers: olaHeaders() }
  );

  const first = response?.results?.[0];
  if (!first) {
    return fallback;
  }

  const label = first.name ?? first.formatted_address ?? fallback.label;
  return {
    id: first.place_id ?? fallback.id,
    label,
    description: first.formatted_address ?? fallback.description,
    latitude: first.geometry?.location?.lat ?? coords.latitude,
    longitude: first.geometry?.location?.lng ?? coords.longitude,
    source: "ola",
  };
}

export const __test__ = {
  FALLBACK_SUGGESTIONS,
  filterFallback,
  normalisePrediction,
};
