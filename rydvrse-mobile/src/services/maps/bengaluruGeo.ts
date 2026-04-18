export type Coordinates = {
  latitude: number;
  longitude: number;
};

const BENGALURU_CENTER: Coordinates = {
  latitude: 12.9716,
  longitude: 77.5946,
};

const KNOWN_POINTS: Array<{ tokens: string[]; coords: Coordinates }> = [
  { tokens: ["koramangala"], coords: { latitude: 12.9352, longitude: 77.6245 } },
  { tokens: ["whitefield"], coords: { latitude: 12.9698, longitude: 77.75 } },
  { tokens: ["indiranagar"], coords: { latitude: 12.9784, longitude: 77.6408 } },
  { tokens: ["hsr"], coords: { latitude: 12.9121, longitude: 77.6446 } },
  { tokens: ["electronic city"], coords: { latitude: 12.8452, longitude: 77.6602 } },
  { tokens: ["mg road"], coords: { latitude: 12.9756, longitude: 77.6064 } },
  { tokens: ["jayanagar"], coords: { latitude: 12.9299, longitude: 77.5833 } },
  { tokens: ["lavelle"], coords: { latitude: 12.9719, longitude: 77.5962 } },
  { tokens: ["airport", "kempegowda"], coords: { latitude: 13.1986, longitude: 77.7066 } },
];

export function inferBengaluruCoords(label?: string | null): Coordinates | null {
  if (!label) {
    return null;
  }
  const normalized = label.toLowerCase();
  const match = KNOWN_POINTS.find((point) =>
    point.tokens.some((token) => normalized.includes(token)),
  );
  return match?.coords ?? null;
}

export function fallbackBengaluruCenter() {
  return BENGALURU_CENTER;
}
