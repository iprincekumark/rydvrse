import React, { useMemo } from "react";
import { Pressable, StyleSheet, View } from "react-native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { colors, radius, semantic, shadows, space } from "@/theme";

type Coordinates = {
  latitude: number;
  longitude: number;
};

type RydvrseMapPreviewProps = {
  pickupLabel: string;
  dropLabel: string;
  serviceType?: string;
  distanceLabel?: string;
  durationLabel?: string;
  onUseCurrentLocation?: () => void;
  onOpenSearch?: () => void;
  onRecenter?: () => void;
  pickupCoords?: Coordinates;
  dropCoords?: Coordinates;
  currentCoords?: Coordinates;
  interactive?: boolean;
  /**
   * When `minimal` is true the floating pickup/drop card and "Use current
   * location" button are hidden so the map can be paired with an external
   * booking sheet that owns those controls.
   */
  minimal?: boolean;
};

function loadReactNativeMaps():
  | {
      MapView: any;
      Marker: any;
      Polyline: any;
    }
  | null {
  try {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const mod = require("react-native-maps");
    const MapView = mod?.default ?? mod?.MapView;
    const Marker = mod?.Marker;
    const Polyline = mod?.Polyline;
    if (!MapView || !Marker) {
      return null;
    }
    return { MapView, Marker, Polyline };
  } catch {
    return null;
  }
}

function resolveRegion(points: Coordinates[]) {
  if (!points.length) {
    return {
      latitude: 12.9716,
      longitude: 77.5946,
      latitudeDelta: 0.12,
      longitudeDelta: 0.12,
    };
  }

  const latitudes = points.map((point) => point.latitude);
  const longitudes = points.map((point) => point.longitude);
  const minLat = Math.min(...latitudes);
  const maxLat = Math.max(...latitudes);
  const minLng = Math.min(...longitudes);
  const maxLng = Math.max(...longitudes);

  const latitudeDelta = Math.max(0.02, (maxLat - minLat) * 1.8);
  const longitudeDelta = Math.max(0.02, (maxLng - minLng) * 1.8);

  return {
    latitude: (minLat + maxLat) / 2,
    longitude: (minLng + maxLng) / 2,
    latitudeDelta,
    longitudeDelta,
  };
}

function StaticMapFallback({ serviceType }: { serviceType: string }) {
  const isRoundTrip = serviceType === "ROUND_TRIP";
  return (
    <>
      <View style={styles.gridLayer}>
        {Array.from({ length: 8 }).map((_, index) => (
          <View key={`h-${index}`} style={[styles.gridLineHorizontal, { top: `${index * 14}%` }]} />
        ))}
        {Array.from({ length: 6 }).map((_, index) => (
          <View key={`v-${index}`} style={[styles.gridLineVertical, { left: `${index * 18}%` }]} />
        ))}
      </View>
      <View style={[styles.road, styles.roadOne]} />
      <View style={[styles.road, styles.roadTwo]} />
      <View style={[styles.road, styles.roadThree]} />
      <View style={styles.routeLine} />
      {isRoundTrip ? <View style={styles.returnRouteLine} /> : null}
      {isRoundTrip ? <View style={styles.returnRouteDash} /> : null}
      <View style={[styles.marker, styles.pickupMarker]}>
        <View style={styles.markerDot} />
      </View>
      <View style={[styles.marker, styles.dropMarker]}>
        <AppIcon name="pin" size={16} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
      </View>
    </>
  );
}

export function RydvrseMapPreview({
  pickupLabel,
  dropLabel,
  serviceType = "ONE_WAY_DROP",
  distanceLabel,
  durationLabel,
  onUseCurrentLocation,
  onOpenSearch,
  onRecenter,
  pickupCoords,
  dropCoords,
  currentCoords,
  interactive = false,
  minimal = false,
}: RydvrseMapPreviewProps) {
  const maps = useMemo(() => (interactive ? loadReactNativeMaps() : null), [interactive]);
  const routePoints = useMemo(
    () => [pickupCoords, dropCoords].filter(Boolean) as Coordinates[],
    [dropCoords, pickupCoords],
  );
  const regionPoints = useMemo(
    () => [pickupCoords, dropCoords, currentCoords].filter(Boolean) as Coordinates[],
    [currentCoords, dropCoords, pickupCoords],
  );
  const initialRegion = useMemo(() => resolveRegion(regionPoints), [regionPoints]);

  const mapKey = useMemo(
    () =>
      `${initialRegion.latitude.toFixed(4)}:${initialRegion.longitude.toFixed(4)}:${initialRegion.latitudeDelta.toFixed(4)}:${initialRegion.longitudeDelta.toFixed(4)}`,
    [initialRegion.latitude, initialRegion.latitudeDelta, initialRegion.longitude, initialRegion.longitudeDelta],
  );

  const MapView = maps?.MapView;
  const Marker = maps?.Marker;
  const Polyline = maps?.Polyline;

  return (
    <View style={styles.mapShell} accessibilityLabel="Rydvrse live map with pickup and drop route">
      {MapView && Marker ? (
        <MapView
          key={mapKey}
          style={StyleSheet.absoluteFillObject}
          initialRegion={initialRegion}
          showsUserLocation
          showsCompass={false}
          showsMyLocationButton={false}
          showsScale={false}
          loadingEnabled
          rotateEnabled={false}
        >
          {pickupCoords ? (
            <Marker coordinate={pickupCoords} title="Pickup" pinColor={semantic.text.primary} />
          ) : null}
          {dropCoords ? (
            <Marker coordinate={dropCoords} title="Drop" pinColor={colors.brand.strong} />
          ) : null}
          {Polyline && routePoints.length >= 2 ? (
            <Polyline coordinates={routePoints} strokeWidth={4} strokeColor={colors.brand.strong} />
          ) : null}
        </MapView>
      ) : (
        <StaticMapFallback serviceType={serviceType} />
      )}

      {durationLabel || distanceLabel ? (
        <View style={styles.etaChip}>
          <AppIcon name="eta" size={14} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          <AppText variant="caption" style={styles.etaChipText}>
            {durationLabel ?? ""}
            {durationLabel && distanceLabel ? " · " : ""}
            {distanceLabel ?? ""}
          </AppText>
        </View>
      ) : null}

      <View style={styles.topControls}>
        <Pressable style={styles.mapPill} onPress={onOpenSearch} accessibilityRole="button" accessibilityLabel="Search pickup or drop location">
          <AppIcon name="route" size={16} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          <AppText variant="caption" style={styles.mapPillText}>
            Bengaluru route preview
          </AppText>
        </Pressable>
        <Pressable style={styles.iconButton} onPress={onRecenter} accessibilityRole="button" accessibilityLabel="Recenter map">
          <AppIcon name="eta" size={18} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
        </Pressable>
      </View>

      {!minimal ? (
        <View style={styles.locationCard}>
          <View style={styles.locationRow}>
            <View style={[styles.locationDot, styles.pickupDot]} />
            <View style={styles.locationText}>
              <AppText variant="caption">Pickup</AppText>
              <AppText variant="bodyStrong" numberOfLines={1}>{pickupLabel}</AppText>
            </View>
          </View>
          <View style={styles.locationDivider} />
          <View style={styles.locationRow}>
            <View style={[styles.locationDot, styles.dropDot]} />
            <View style={styles.locationText}>
              <AppText variant="caption">Drop</AppText>
              <AppText variant="bodyStrong" numberOfLines={1}>{dropLabel}</AppText>
            </View>
          </View>
        </View>
      ) : null}

      {!minimal && onUseCurrentLocation ? (
        <Pressable style={styles.currentLocationButton} onPress={onUseCurrentLocation} accessibilityRole="button" accessibilityLabel="Use current location">
          <AppIcon name="pin" size={17} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
          <AppText variant="caption" style={styles.currentLocationText}>Use current location</AppText>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  mapShell: {
    flex: 1,
    minHeight: 230,
    backgroundColor: "#E9EFDF",
    overflow: "hidden",
  },
  gridLayer: {
    ...StyleSheet.absoluteFillObject,
    opacity: 0.45,
  },
  gridLineHorizontal: {
    position: "absolute",
    left: 0,
    right: 0,
    height: 1,
    backgroundColor: "rgba(16, 19, 18, 0.06)",
  },
  gridLineVertical: {
    position: "absolute",
    top: 0,
    bottom: 0,
    width: 1,
    backgroundColor: "rgba(16, 19, 18, 0.06)",
  },
  road: {
    position: "absolute",
    height: 18,
    borderRadius: radius.full,
    backgroundColor: "rgba(255,255,255,0.88)",
    borderWidth: 1,
    borderColor: "rgba(16, 19, 18, 0.06)",
  },
  roadOne: {
    width: "86%",
    top: "28%",
    left: "-8%",
    transform: [{ rotate: "-18deg" }],
  },
  roadTwo: {
    width: "96%",
    top: "56%",
    right: "-20%",
    transform: [{ rotate: "22deg" }],
  },
  roadThree: {
    width: "65%",
    top: "42%",
    left: "12%",
    transform: [{ rotate: "88deg" }],
  },
  routeLine: {
    position: "absolute",
    width: "58%",
    height: 6,
    top: "43%",
    left: "22%",
    borderRadius: radius.full,
    backgroundColor: colors.brand.strong,
    transform: [{ rotate: "-32deg" }],
  },
  returnRouteLine: {
    position: "absolute",
    width: "58%",
    height: 5,
    top: "50%",
    left: "22%",
    borderRadius: radius.full,
    backgroundColor: "rgba(143, 214, 47, 0.35)",
    transform: [{ rotate: "-32deg" }],
  },
  returnRouteDash: {
    position: "absolute",
    width: "20%",
    height: 3,
    top: "34%",
    left: "40%",
    borderRadius: radius.full,
    backgroundColor: colors.brand.primary,
    transform: [{ rotate: "12deg" }],
  },
  etaChip: {
    position: "absolute",
    top: space[4] + 48,
    alignSelf: "center",
    left: "50%",
    marginLeft: -70,
    minWidth: 140,
    height: 30,
    borderRadius: radius.full,
    backgroundColor: "rgba(255,255,255,0.95)",
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: space[1],
    paddingHorizontal: space[3],
    ...shadows.sm,
  },
  etaChipText: {
    color: semantic.text.primary,
  },
  marker: {
    position: "absolute",
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 3,
    borderColor: semantic.bg.surface,
    ...shadows.md,
  },
  pickupMarker: {
    left: "19%",
    top: "55%",
    backgroundColor: semantic.text.primary,
  },
  dropMarker: {
    right: "18%",
    top: "27%",
    backgroundColor: colors.brand.primary,
  },
  markerDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: colors.brand.primary,
  },
  topControls: {
    position: "absolute",
    top: space[4],
    left: space[4],
    right: space[4],
    flexDirection: "row",
    justifyContent: "space-between",
    gap: space[3],
  },
  mapPill: {
    flex: 1,
    minHeight: 42,
    borderRadius: radius.full,
    backgroundColor: "rgba(255,255,255,0.92)",
    flexDirection: "row",
    alignItems: "center",
    gap: space[2],
    paddingHorizontal: space[4],
    ...shadows.sm,
  },
  mapPillText: {
    color: semantic.text.primary,
    flexShrink: 1,
  },
  iconButton: {
    width: 42,
    height: 42,
    borderRadius: radius.full,
    backgroundColor: "rgba(255,255,255,0.92)",
    alignItems: "center",
    justifyContent: "center",
    ...shadows.sm,
  },
  locationCard: {
    position: "absolute",
    left: space[4],
    right: space[4],
    bottom: 80,
    borderRadius: radius.lg,
    backgroundColor: "rgba(255,255,255,0.94)",
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: space[3],
    gap: space[2],
    ...shadows.md,
  },
  locationRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: space[3],
  },
  locationDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  pickupDot: {
    backgroundColor: semantic.text.primary,
  },
  dropDot: {
    backgroundColor: colors.brand.strong,
  },
  locationText: {
    flex: 1,
    gap: 2,
  },
  locationDivider: {
    height: 1,
    backgroundColor: semantic.border.soft,
    marginLeft: 15,
  },
  currentLocationButton: {
    position: "absolute",
    right: space[4],
    bottom: 24,
    minHeight: 34,
    borderRadius: radius.full,
    backgroundColor: colors.brand.primary,
    flexDirection: "row",
    alignItems: "center",
    gap: space[2],
    paddingHorizontal: space[3],
    ...shadows.sm,
  },
  currentLocationText: {
    color: semantic.text.onBrand,
  },
});
