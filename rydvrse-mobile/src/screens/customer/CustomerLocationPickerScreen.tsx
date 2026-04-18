import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { ActivityIndicator, FlatList, Pressable, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { Screen } from "@/components/layout/Screen";
import { TextField } from "@/components/forms/TextField";
import { StatusBanner } from "@/components/common/StatusBanner";
import { useCurrentLocation } from "@/hooks/useCurrentLocation";
import { autocompletePlaces, PlaceSuggestion, reverseGeocode } from "@/services/maps/olaPlacesService";
import { useAppDispatch, useAppSelector } from "@/store";
import { updateBookingForm } from "@/store/customerSlice";
import { colors, radius, semantic, shadows, space, spacing } from "@/theme";

type PickerField = "pickup" | "drop";

const DEBOUNCE_MS = 250;

/**
 * A focused location search screen used both for selecting pickup and drop
 * locations. It wraps the Ola Maps autocomplete service with a "current
 * location" shortcut and a recent-places section so the user has a fast path
 * regardless of whether the network call succeeds.
 */
export function CustomerLocationPickerScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const dispatch = useAppDispatch();
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);

  const field: PickerField = route.params?.field === "drop" ? "drop" : "pickup";
  const seed: string = (route.params?.seed as string | undefined) ?? bookingForm[field] ?? "";
  const title = field === "pickup" ? "Set pickup" : "Set drop";
  const placeholder =
    field === "pickup" ? "Search pickup location" : "Search drop location";

  const [query, setQuery] = useState(seed);
  const [results, setResults] = useState<PlaceSuggestion[]>([]);
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const queryRef = useRef(seed);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const { coords, usingFallback, status, refresh } = useCurrentLocation({ autoRequest: true });

  const runSearch = useCallback(
    async (term: string) => {
      try {
        setLoading(true);
        setError(null);
        const suggestions = await autocompletePlaces(term, {
          biasCoords: usingFallback ? undefined : coords,
        });
        if (queryRef.current !== term) {
          return;
        }
        setResults(suggestions);
      } catch (searchError) {
        setError(
          searchError instanceof Error
            ? searchError.message
            : "Search is temporarily unavailable."
        );
      } finally {
        if (queryRef.current === term) {
          setLoading(false);
        }
      }
    },
    [coords, usingFallback]
  );

  useEffect(() => {
    queryRef.current = query;
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      void runSearch(query);
    }, DEBOUNCE_MS);

    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [query, runSearch]);

  const handleSelect = useCallback(
    (suggestion: PlaceSuggestion) => {
      dispatch(updateBookingForm({ [field]: suggestion.label }));
      navigation.goBack();
    },
    [dispatch, field, navigation]
  );

  const handleUseCurrentLocation = useCallback(async () => {
    try {
      setLocating(true);
      setError(null);

      if (usingFallback) {
        await refresh();
      }

      const reverse = await reverseGeocode(coords);
      handleSelect(reverse);
    } catch (locateError) {
      setError(
        locateError instanceof Error
          ? locateError.message
          : "We couldn't read your current location."
      );
    } finally {
      setLocating(false);
    }
  }, [coords, handleSelect, refresh, usingFallback]);

  const bannerMessage = useMemo(() => {
    if (status === "denied") {
      return "Location permission denied — you can still search for a place.";
    }
    if (status === "unavailable") {
      return "Device location unavailable — results are centred on Bengaluru.";
    }
    return null;
  }, [status]);

  return (
    <Screen backgroundColor={semantic.bg.app}>
      <View style={styles.header}>
        <Pressable
          onPress={() => navigation.goBack()}
          style={styles.backButton}
          accessibilityRole="button"
          accessibilityLabel="Go back"
        >
          <AppIcon name="arrowLeft" size={20} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
        </Pressable>
        <View style={styles.headerCopy}>
          <AppText variant="section">{title}</AppText>
          <AppText variant="caption">Tap a result to continue.</AppText>
        </View>
      </View>

      <View style={styles.searchWrap}>
        <TextField
          label={placeholder}
          value={query}
          onChangeText={setQuery}
          icon={field === "pickup" ? "pin" : "route"}
          helperText={loading ? "Searching..." : "Powered by Ola Maps and your recent places."}
        />
      </View>

      <Pressable
        onPress={handleUseCurrentLocation}
        style={({ pressed }) => [styles.currentRow, pressed && { opacity: 0.85 }]}
        disabled={locating}
        accessibilityRole="button"
        accessibilityLabel="Use current location"
      >
        <View style={styles.currentIcon}>
          {locating ? (
            <ActivityIndicator color={colors.brand.ink} />
          ) : (
            <AppIcon name="pin" size={18} color={colors.brand.ink} secondaryColor={colors.brand.ink} />
          )}
        </View>
        <View style={styles.currentCopy}>
          <AppText variant="bodyStrong">Use current location</AppText>
          <AppText variant="caption">
            {usingFallback ? "Bengaluru default — enable GPS for a precise fix." : "GPS fix ready."}
          </AppText>
        </View>
        <AppIcon name="arrowRight" size={18} color={colors.brand.ink} secondaryColor={colors.brand.ink} />
      </Pressable>

      {bannerMessage ? <StatusBanner tone="info" title="Heads up" message={bannerMessage} /> : null}
      {error ? <StatusBanner tone="warning" title="Search notice" message={error} /> : null}

      <FlatList
        data={results}
        keyExtractor={(item) => item.id}
        keyboardShouldPersistTaps="handled"
        style={styles.list}
        contentContainerStyle={styles.listContent}
        ListHeaderComponent={
          results.length ? (
            <AppText variant="caption" style={styles.listHeader}>
              {loading ? "Updating suggestions..." : "Matching places"}
            </AppText>
          ) : null
        }
        ListEmptyComponent={
          loading ? null : (
            <View style={styles.emptyState}>
              <AppText variant="bodyStrong">No matches yet</AppText>
              <AppText variant="caption">
                Try a landmark, mall, metro station, or street name.
              </AppText>
            </View>
          )
        }
        renderItem={({ item }) => (
          <Pressable
            onPress={() => handleSelect(item)}
            style={({ pressed }) => [styles.resultRow, pressed && { opacity: 0.8 }]}
            accessibilityRole="button"
            accessibilityLabel={`Select ${item.label}`}
          >
            <View style={styles.resultIcon}>
              <AppIcon
                name={item.source === "recent" ? "clock" : field === "pickup" ? "pin" : "route"}
                size={18}
                color={colors.brand.primary}
                secondaryColor={colors.neutral[400]}
              />
            </View>
            <View style={styles.resultCopy}>
              <AppText variant="bodyStrong" numberOfLines={1}>
                {item.label}
              </AppText>
              {item.description ? (
                <AppText variant="caption" numberOfLines={1}>
                  {item.description}
                </AppText>
              ) : null}
            </View>
            <AppIcon name="arrowRight" size={16} color={colors.neutral[400]} secondaryColor={colors.neutral[400]} />
          </Pressable>
        )}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  header: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    marginBottom: spacing.md,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  headerCopy: {
    flex: 1,
    gap: 2,
  },
  searchWrap: {
    marginBottom: spacing.sm,
  },
  currentRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: radius.lg,
    backgroundColor: colors.brand.primary,
    marginBottom: spacing.md,
    ...shadows.sm,
  },
  currentIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: "rgba(16, 19, 18, 0.08)",
    alignItems: "center",
    justifyContent: "center",
  },
  currentCopy: {
    flex: 1,
    gap: 2,
  },
  list: {
    flex: 1,
  },
  listContent: {
    paddingBottom: space[8],
    gap: spacing.xs,
  },
  listHeader: {
    marginBottom: spacing.xs,
    textTransform: "uppercase",
    letterSpacing: 1,
  },
  emptyState: {
    padding: spacing.lg,
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    gap: spacing.xs,
  },
  resultRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  resultIcon: {
    width: 40,
    height: 40,
    borderRadius: 14,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  resultCopy: {
    flex: 1,
    gap: 2,
  },
});
