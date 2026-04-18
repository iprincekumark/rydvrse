import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, ScrollView, StyleSheet, View, useWindowDimensions } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import { AppIcon } from "@/assets/icons/AppIcon";
import { ServiceIcon } from "@/assets/icons/ServiceIcon";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { BookingHomeSheet } from "@/components/booking/BookingHomeSheet";
import { HeaderBlock } from "@/components/layout/HeaderBlock";
import { ScreenHeader } from "@/components/layout/ScreenHeader";
import { Screen } from "@/components/layout/Screen";
import { AppText } from "@/components/common/AppText";
import { TextField } from "@/components/forms/TextField";
import { SectionCard } from "@/components/cards/SectionCard";
import { ChoiceCard } from "@/components/common/ChoiceCard";
import { StatusBanner } from "@/components/common/StatusBanner";
import { StatusChip } from "@/components/common/StatusChip";
import { KeyValueRow } from "@/components/common/KeyValueRow";
import { EmptyState } from "@/components/common/EmptyState";
import { Skeleton } from "@/components/loaders/Skeleton";
import { FareBreakdown } from "@/components/patterns/FareBreakdown";
import { RydvrseMapPreview } from "@/components/maps/RydvrseMapPreview";
import { SUPPORT_FAQS, SupportFaq, searchFaqs } from "@/constants/supportFaq";
import { CustomerServiceType } from "@/constants/serviceTypes";
import { authApi } from "@/services/api/auth";
import { customerApi } from "@/services/api/customer";
import { calculateRouteEstimate } from "@/services/maps/routeEstimator";
import { inferBengaluruCoords } from "@/services/maps/bengaluruGeo";
import { reverseGeocode } from "@/services/maps/olaPlacesService";
import { useCurrentLocation } from "@/hooks/useCurrentLocation";
import { useAppDispatch, useAppSelector } from "@/store";
import { markProfileComplete, setActiveBooking, setBookings, setQuote, updateBookingForm } from "@/store/customerSlice";
import { hydrateSession, logout } from "@/store/sessionSlice";
import { colors, semantic, spacing } from "@/theme";
import { formatCompactTime, formatCurrency } from "@/utils/format";

const BENGALURU_CITY_ID = "20000000-0000-0000-0000-000000000001";

function toApiServiceType(serviceType: CustomerServiceType) {
  if (serviceType === "ONE_WAY_DROP") return "SCHEDULED_ONE_WAY";
  if (serviceType === "ROUND_TRIP") return "SCHEDULED_ROUND_TRIP";
  return serviceType;
}

function toUiServiceType(serviceType: string): CustomerServiceType {
  if (serviceType === "SCHEDULED_ONE_WAY") return "ONE_WAY_DROP";
  if (serviceType === "SCHEDULED_ROUND_TRIP") return "ROUND_TRIP";
  return serviceType as CustomerServiceType;
}

function displayServiceType(serviceType: string) {
  return toUiServiceType(serviceType).replaceAll("_", " ");
}

function parsePositiveInt(value: string, fallback: number) {
  const parsed = Number.parseInt(value.replace(/[^0-9]/g, ""), 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function scheduleIso(kind: "now" | "thirty" | "hour") {
  const now = new Date();
  if (kind === "now") {
    return now.toISOString();
  }
  if (kind === "thirty") {
    return new Date(now.getTime() + 30 * 60 * 1000).toISOString();
  }
  return new Date(now.getTime() + 60 * 60 * 1000).toISOString();
}

async function refreshRouteEstimate(
  dispatch: ReturnType<typeof useAppDispatch>,
  bookingForm: {
    pickup: string;
    drop: string;
    pickupLatitude: number | null;
    pickupLongitude: number | null;
    dropLatitude: number | null;
    dropLongitude: number | null;
    serviceType: CustomerServiceType;
  },
) {
  const estimate = await calculateRouteEstimate({
    pickupLabel: bookingForm.pickup,
    dropLabel: bookingForm.drop,
    serviceType: bookingForm.serviceType,
    pickupCoords:
      bookingForm.pickupLatitude != null && bookingForm.pickupLongitude != null
        ? {
            latitude: bookingForm.pickupLatitude,
            longitude: bookingForm.pickupLongitude,
          }
        : null,
    dropCoords:
      bookingForm.dropLatitude != null && bookingForm.dropLongitude != null
        ? {
            latitude: bookingForm.dropLatitude,
            longitude: bookingForm.dropLongitude,
          }
        : null,
  });

  dispatch(
    updateBookingForm({
      distanceKm: String(estimate.roundedDistanceKm),
      predictedDriveMinutes: String(estimate.predictedDriveMinutes),
      driverPickupDistanceKm: String(estimate.driverPickupDistanceKm),
      driverPickupEtaMinutes: String(estimate.driverPickupEtaMinutes),
      durationLabel: `${estimate.predictedDriveMinutes} mins`,
    }),
  );
}

function resolveStatusTone(status: string): "info" | "success" | "warning" | "neutral" {
  const normalized = status.toLowerCase();

  if (normalized.includes("completed") || normalized.includes("confirmed") || normalized.includes("assigned")) {
    return "success";
  }

  if (normalized.includes("pending") || normalized.includes("requested") || normalized.includes("created")) {
    return "info";
  }

  if (normalized.includes("cancel") || normalized.includes("delay") || normalized.includes("issue")) {
    return "warning";
  }

  return "neutral";
}

function SectionTitle({ label }: { label: string }) {
  return (
    <AppText variant="section" style={{ marginBottom: spacing.sm }}>
      {label}
    </AppText>
  );
}

function BookingCard({
  title,
  subtitle,
  amount,
  serviceType,
  status,
  onPress
}: {
  title: string;
  subtitle: string;
  amount: string;
  serviceType: string;
  status: string;
  onPress?: () => void;
}) {
  return (
    <Pressable onPress={onPress} accessibilityRole={onPress ? "button" : undefined} accessibilityLabel={title}>
      <SectionCard>
        <View style={styles.bookingRow}>
          <View style={styles.bookingMain}>
            <View style={styles.bookingIconWrap}>
              <ServiceIcon serviceType={toUiServiceType(serviceType)} />
            </View>
            <View style={styles.bookingContent}>
              <View style={styles.bookingHeader}>
                <AppText variant="section">{title}</AppText>
                <StatusChip label={status.replaceAll("_", " ")} tone={resolveStatusTone(status)} />
              </View>
              <AppText variant="body">{subtitle}</AppText>
            </View>
          </View>
          <View style={styles.bookingAside}>
            <AppText variant="bodyStrong">{amount}</AppText>
            <AppText variant="caption" style={styles.bookingCaption}>
              Fare snapshot
            </AppText>
          </View>
        </View>
      </SectionCard>
    </Pressable>
  );
}

function CustomerSignalStrip() {
  return (
    <SectionCard>
      <View style={styles.signalStrip}>
        {[
          { icon: "shield" as const, title: "Trust-first", subtitle: "Verified drivers" },
          { icon: "wallet" as const, title: "Clear fare", subtitle: "Visible breakdown" },
          { icon: "driver" as const, title: "Backup ready", subtitle: "Rescue on drift" }
        ].map((item) => (
          <View key={item.title} style={styles.signalCard}>
            <View style={styles.signalIconWrap}>
              <AppIcon name={item.icon} size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
            </View>
            <View style={styles.signalText}>
              <AppText variant="bodyStrong">{item.title}</AppText>
              <AppText variant="caption">{item.subtitle}</AppText>
            </View>
          </View>
        ))}
      </View>
    </SectionCard>
  );
}

function QuoteSummaryStrip({ serviceType, amount }: { serviceType: string; amount: string }) {
  return (
    <View style={styles.metricStrip}>
      <View style={styles.metricCard}>
        <View style={styles.metricIconWrap}>
          <ServiceIcon serviceType={toUiServiceType(serviceType)} />
        </View>
        <AppText variant="caption">Service</AppText>
        <AppText variant="bodyStrong">{displayServiceType(serviceType)}</AppText>
      </View>
      <View style={styles.metricCard}>
        <View style={styles.metricIconWrap}>
          <AppIcon name="wallet" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
        </View>
        <AppText variant="caption">Estimated total</AppText>
        <AppText variant="bodyStrong">{amount}</AppText>
      </View>
      <View style={styles.metricCard}>
        <View style={styles.metricIconWrap}>
          <AppIcon name="shield" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
        </View>
        <AppText variant="caption">Billing mode</AppText>
        <AppText variant="bodyStrong">Visible upfront</AppText>
      </View>
    </View>
  );
}

function DriverTrustStrip() {
  return (
    <View style={styles.metricStrip}>
      {[
        { icon: "driver" as const, label: "Driver", value: "Arun K" },
        { icon: "star" as const, label: "Rating", value: "4.9" },
        { icon: "eta" as const, label: "ETA", value: "17 mins" }
      ].map((item) => (
        <View key={item.label} style={styles.metricCard}>
          <View style={styles.metricIconWrap}>
            <AppIcon name={item.icon} size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
          </View>
          <AppText variant="caption">{item.label}</AppText>
          <AppText variant="bodyStrong">{item.value}</AppText>
        </View>
      ))}
    </View>
  );
}

function TripMetricRow() {
  return (
    <View style={styles.metricStrip}>
      {[
        { icon: "clock" as const, label: "Trip timer", value: "00:24" },
        { icon: "route" as const, label: "Route status", value: "On track" },
        { icon: "driver" as const, label: "Driver", value: "Arun K" }
      ].map((item) => (
        <View key={item.label} style={styles.metricCard}>
          <View style={styles.metricIconWrap}>
            <AppIcon name={item.icon} size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
          </View>
          <AppText variant="caption">{item.label}</AppText>
          <AppText variant="bodyStrong">{item.value}</AppText>
        </View>
      ))}
    </View>
  );
}

export function CustomerLoginScreen() {
  const navigation = useNavigation<any>();
  const [phone, setPhone] = useState("+919999999999");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleContinue = async () => {
    try {
      setLoading(true);
      setError("");
      await authApi.requestOtp("CUSTOMER", phone);
      navigation.navigate("CustomerOtp", { phone });
    } catch (requestError) {
      setError("We couldn't send the OTP right now. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Customer" title="Book a verified driver." subtitle="Login with mobile number." visualVariant="customer" />
      <View style={styles.stackLg}>
        <TextField label="Mobile number" value={phone} onChangeText={setPhone} keyboardType="phone-pad" icon="phone" helperText={error || "OTP login."} />
        <BottomActionBar primaryLabel={loading ? "Sending OTP..." : "Continue"} onPrimaryPress={handleContinue} primaryDisabled={loading || phone.length < 10} primaryIcon="arrowRight" />
      </View>
    </Screen>
  );
}

export function CustomerOtpScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const dispatch = useAppDispatch();
  const [otp, setOtp] = useState("123456");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const phone = route.params?.phone ?? "+919999999999";

  const handleVerify = async () => {
    try {
      setSubmitting(true);
      setError("");
      const session = await authApi.verifyOtp("CUSTOMER", phone, otp);
      dispatch(hydrateSession(session.data));
      navigation.navigate("CustomerProfileSetup");
    } catch (verifyError) {
      setError("Incorrect or expired OTP. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Verify" title="Confirm your number" subtitle={`We sent a six-digit code to ${phone}.`} visualVariant="trust" />
      <View style={styles.stackLg}>
        <TextField label="OTP code" value={otp} onChangeText={setOtp} keyboardType="numeric" icon="ticket" helperText={error || "Use 123456 in mock mode."} />
        <BottomActionBar primaryLabel={submitting ? "Verifying..." : "Verify OTP"} secondaryLabel="Change number" onPrimaryPress={handleVerify} onSecondaryPress={() => navigation.goBack()} primaryDisabled={submitting || otp.length < 6} primaryIcon="check" secondaryIcon="phone" />
      </View>
    </Screen>
  );
}

export function CustomerProfileSetupScreen() {
  const navigation = useNavigation<any>();
  const dispatch = useAppDispatch();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const [fullName, setFullName] = useState("Meera Singh");
  const [email, setEmail] = useState("meera@rydvrse.app");
  const [city, setCity] = useState("Bengaluru");
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    if (!accessToken) {
      return;
    }

    setSaving(true);
    await customerApi.updateProfile(accessToken, {
      full_name: fullName,
      email,
      city_name: city
    });
    dispatch(markProfileComplete());
    setSaving(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Setup" title="Complete profile." subtitle="Used for bookings and support." visualVariant="trust" />
      <View style={styles.stackMd}>
        <TextField label="Full name" value={fullName} onChangeText={setFullName} icon="profile" />
        <TextField label="Email (optional)" value={email} onChangeText={setEmail} icon="document" />
        <TextField label="Launch city" value={city} onChangeText={setCity} icon="city" />
        <StatusBanner tone="info" title="Manual entry works." message="Location permission is optional." />
        <BottomActionBar primaryLabel={saving ? "Saving..." : "Continue"} onPrimaryPress={handleSave} primaryDisabled={saving || !fullName.trim()} primaryIcon="check" />
      </View>
    </Screen>
  );
}

export function CustomerHomeScreen() {
  const navigation = useNavigation<any>();
  const { width, height } = useWindowDimensions();
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);
  const dispatch = useAppDispatch();
  const insets = useSafeAreaInsets();
  const isWide = width >= 720;
  const mapHeight = isWide
    ? Math.max(520, height - 120)
    : Math.max(280, Math.min(360, height * 0.42));
  const { coords, usingFallback, refresh, status } = useCurrentLocation({
    autoRequest: true,
    liveUpdates: true,
  });
  const [resolvingPickup, setResolvingPickup] = useState(false);
  const [pickupSeeded, setPickupSeeded] = useState(false);

  const distanceLabel = useMemo(() => {
    const km = parsePositiveInt(bookingForm.distanceKm, 0);
    return km ? `${km} km` : undefined;
  }, [bookingForm.distanceKm]);
  const durationLabel = useMemo(() => {
    const mins = parsePositiveInt(bookingForm.predictedDriveMinutes, 0);
    return mins ? `${mins} min` : undefined;
  }, [bookingForm.predictedDriveMinutes]);

  useEffect(() => {
    let active = true;

    const estimateRoute = async () => {
      const estimate = await calculateRouteEstimate({
        pickupLabel: bookingForm.pickup,
        dropLabel: bookingForm.drop,
        serviceType: bookingForm.serviceType,
        pickupCoords:
          bookingForm.pickupLatitude != null && bookingForm.pickupLongitude != null
            ? {
                latitude: bookingForm.pickupLatitude,
                longitude: bookingForm.pickupLongitude,
              }
            : null,
        dropCoords:
          bookingForm.dropLatitude != null && bookingForm.dropLongitude != null
            ? {
                latitude: bookingForm.dropLatitude,
                longitude: bookingForm.dropLongitude,
              }
            : null,
      });

      if (!active) {
        return;
      }

      dispatch(
        updateBookingForm({
          distanceKm: String(estimate.roundedDistanceKm),
          predictedDriveMinutes: String(estimate.predictedDriveMinutes),
          driverPickupDistanceKm: String(estimate.driverPickupDistanceKm),
          driverPickupEtaMinutes: String(estimate.driverPickupEtaMinutes),
          durationLabel: `${estimate.predictedDriveMinutes} mins`,
        }),
      );
    };

    void estimateRoute();

    return () => {
      active = false;
    };
  }, [
    bookingForm.drop,
    bookingForm.dropLatitude,
    bookingForm.dropLongitude,
    bookingForm.pickup,
    bookingForm.pickupLatitude,
    bookingForm.pickupLongitude,
    bookingForm.serviceType,
    dispatch,
  ]);

  useEffect(() => {
    let active = true;

    const hydratePickupFromGps = async () => {
      if (pickupSeeded || status !== "granted" || usingFallback) {
        return;
      }
      const reverse = await reverseGeocode(coords);
      if (!active) {
        return;
      }
      dispatch(
        updateBookingForm({
          pickup: reverse.label,
          pickupLatitude: reverse.latitude ?? coords.latitude,
          pickupLongitude: reverse.longitude ?? coords.longitude,
        }),
      );
      setPickupSeeded(true);
    };

    void hydratePickupFromGps();

    return () => {
      active = false;
    };
  }, [coords, dispatch, pickupSeeded, status, usingFallback]);

  const handleUseCurrentLocation = useCallback(async () => {
    if (resolvingPickup) {
      return;
    }
    try {
      setResolvingPickup(true);
      if (usingFallback) {
        await refresh();
      }
      const reverse = await reverseGeocode(coords);
      dispatch(
        updateBookingForm({
          pickup: reverse.label,
          pickupLatitude: reverse.latitude ?? coords.latitude,
          pickupLongitude: reverse.longitude ?? coords.longitude,
        }),
      );
    } finally {
      setResolvingPickup(false);
    }
  }, [coords, dispatch, refresh, resolvingPickup, usingFallback]);

  const handleOpenLocationSearch = useCallback(
    (field: "pickup" | "drop") => {
      navigation.navigate("CustomerLocationPicker", {
        field,
        seed: field === "pickup" ? bookingForm.pickup : bookingForm.drop,
      });
    },
    [bookingForm.drop, bookingForm.pickup, navigation]
  );

  const handleGetFare = useCallback(async () => {
    await refreshRouteEstimate(dispatch, bookingForm);
    navigation.navigate("CustomerServiceSetup");
  }, [bookingForm, dispatch, navigation]);

  return (
    <Screen padded={false} scrollable={false} variant="map" backgroundColor={semantic.bg.app}>
      <View style={[styles.homeCanvas, isWide && styles.homeCanvasWide]}>
        <View
          style={[
            styles.homeMapPanel,
            { height: mapHeight, paddingTop: insets.top },
            isWide && styles.homeMapPanelWide,
          ]}
        >
          <RydvrseMapPreview
            pickupLabel={bookingForm.pickup}
            dropLabel={bookingForm.drop}
            serviceType={bookingForm.serviceType}
            distanceLabel={distanceLabel}
            durationLabel={durationLabel}
            pickupCoords={
              bookingForm.pickupLatitude != null && bookingForm.pickupLongitude != null
                ? {
                    latitude: bookingForm.pickupLatitude,
                    longitude: bookingForm.pickupLongitude,
                  }
                : undefined
            }
            dropCoords={
              bookingForm.dropLatitude != null && bookingForm.dropLongitude != null
                ? {
                    latitude: bookingForm.dropLatitude,
                    longitude: bookingForm.dropLongitude,
                  }
                : undefined
            }
            currentCoords={coords}
            onOpenSearch={() => handleOpenLocationSearch("pickup")}
            onRecenter={() => {
              void handleUseCurrentLocation();
            }}
            interactive
            minimal
          />
        </View>

        <View
          style={[
            styles.homeSheetPanel,
            { paddingBottom: Math.max(insets.bottom, 12) + 8 },
            isWide && styles.homeSheetPanelWide,
          ]}
        >
          <BookingHomeSheet
            serviceType={bookingForm.serviceType}
            pickup={bookingForm.pickup}
            drop={bookingForm.drop}
            scheduleLabel={formatCompactTime(bookingForm.scheduleAt)}
            onTripTypeChange={(serviceType) => dispatch(updateBookingForm({ serviceType }))}
            onQuickSchedule={(kind) => dispatch(updateBookingForm({ scheduleAt: scheduleIso(kind) }))}
            onOpenPickup={() => handleOpenLocationSearch("pickup")}
            onOpenDrop={() => handleOpenLocationSearch("drop")}
            distanceLabel={distanceLabel}
            durationLabel={durationLabel}
            onGetFare={handleGetFare}
          />
        </View>
      </View>
    </Screen>
  );
}

const TRANSMISSION_OPTIONS: Array<{ id: string; title: string; subtitle: string }> = [
  { id: "AUTOMATIC", title: "Automatic", subtitle: "Self-shifting gearbox" },
  { id: "MANUAL", title: "Manual", subtitle: "Stick shift" },
];

const CAR_TYPE_OPTIONS: Array<{ id: string; title: string; subtitle: string }> = [
  { id: "HATCHBACK", title: "Hatchback", subtitle: "Compact city car" },
  { id: "SEDAN", title: "Sedan", subtitle: "Comfortable 4-door" },
  { id: "SUV", title: "SUV", subtitle: "Spacious, higher stance" },
];

export function CustomerServiceSetupScreen() {
  const navigation = useNavigation<any>();
  const dispatch = useAppDispatch();
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);
  const [estimating, setEstimating] = useState(false);

  const handleContinue = async () => {
    setEstimating(true);
    await refreshRouteEstimate(dispatch, bookingForm);
    setEstimating(false);
    navigation.navigate("CustomerQuote");
  };

  const tripTypeLabel =
    bookingForm.serviceType === "ROUND_TRIP" ? "Round trip" : "One-way drop";

  return (
    <Screen>
      <ScreenHeader
        title="Car details"
        subtitle={`${tripTypeLabel} · ${bookingForm.pickup} → ${bookingForm.drop}`}
        onBack={() => navigation.goBack()}
      />

      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <SectionTitle label="Transmission" />
            <View style={styles.optionGrid}>
              {TRANSMISSION_OPTIONS.map((option) => (
                <ChoiceCard
                  key={option.id}
                  title={option.title}
                  subtitle={option.subtitle}
                  selected={bookingForm.transmissionType === option.id}
                  onPress={() => dispatch(updateBookingForm({ transmissionType: option.id }))}
                />
              ))}
            </View>
          </View>
        </SectionCard>

        <SectionCard>
          <View style={styles.stackSm}>
            <SectionTitle label="Car type" />
            <View style={styles.optionGrid}>
              {CAR_TYPE_OPTIONS.map((option) => (
                <ChoiceCard
                  key={option.id}
                  title={option.title}
                  subtitle={option.subtitle}
                  selected={bookingForm.carType === option.id}
                  onPress={() => dispatch(updateBookingForm({ carType: option.id }))}
                />
              ))}
            </View>
          </View>
        </SectionCard>

        <SectionCard>
          <View style={styles.stackSm}>
            <SectionTitle label="Vehicle" />
            <TextField
              label="Brand and model"
              placeholder="e.g. Maruti Swift"
              value={bookingForm.carBrandModel}
              onChangeText={(value) => dispatch(updateBookingForm({ carBrandModel: value }))}
              icon="document"
            />
            <TextField
              label="Car number"
              placeholder="e.g. KA 05 AB 1234"
              value={bookingForm.carNumber}
              onChangeText={(value) => dispatch(updateBookingForm({ carNumber: value.toUpperCase() }))}
              icon="ticket"
            />
          </View>
        </SectionCard>

        <TextField
          label="Notes"
          placeholder="Anything the driver should know?"
          value={bookingForm.instructions}
          onChangeText={(value) => dispatch(updateBookingForm({ instructions: value }))}
          icon="document"
          multiline
        />

        <BottomActionBar
          primaryLabel={estimating ? "Calculating..." : "Get fare"}
          secondaryLabel="Back"
          onPrimaryPress={handleContinue}
          onSecondaryPress={() => navigation.goBack()}
          primaryDisabled={
            estimating ||
            !bookingForm.transmissionType ||
            !bookingForm.carType ||
            !bookingForm.carBrandModel ||
            !bookingForm.carNumber
          }
          primaryIcon="spark"
          secondaryIcon="arrowLeft"
        />
      </View>
    </Screen>
  );
}

export function CustomerQuoteScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);
  const quote = useAppSelector((state) => state.customer.currentQuote);
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(!quote);

  useEffect(() => {
    let active = true;
    const load = async () => {
      if (!accessToken) {
        return;
      }
      setLoading(true);
      const roundedDistanceKm = parsePositiveInt(bookingForm.distanceKm, 31);
      const predictedDriveMinutes = parsePositiveInt(bookingForm.predictedDriveMinutes, 105);
      const inferredPickup = inferBengaluruCoords(bookingForm.pickup);
      const inferredDrop = inferBengaluruCoords(bookingForm.drop);
      const response = await customerApi.quote(accessToken, {
        service_type: toApiServiceType(bookingForm.serviceType),
        pickup: {
          label: bookingForm.pickup,
          address_line_1: bookingForm.pickup,
          city_id: BENGALURU_CITY_ID,
          latitude: bookingForm.pickupLatitude ?? inferredPickup?.latitude ?? 12.9352,
          longitude: bookingForm.pickupLongitude ?? inferredPickup?.longitude ?? 77.6245
        },
        drop: {
          label: bookingForm.drop,
          address_line_1: bookingForm.drop,
          city_id: BENGALURU_CITY_ID,
          latitude: bookingForm.dropLatitude ?? inferredDrop?.latitude ?? 12.9698,
          longitude: bookingForm.dropLongitude ?? inferredDrop?.longitude ?? 77.75
        },
        scheduled_pickup_at: bookingForm.scheduleAt,
        expected_duration_minutes: predictedDriveMinutes,
        rounded_distance_km: roundedDistanceKm,
        predicted_drive_minutes: predictedDriveMinutes,
        driver_pickup_distance_km: parsePositiveInt(bookingForm.driverPickupDistanceKm, 8),
        driver_pickup_eta_minutes: parsePositiveInt(bookingForm.driverPickupEtaMinutes, 24),
        transmission_type: bookingForm.transmissionType,
        car_type: bookingForm.carType,
        car_brand_model: bookingForm.carBrandModel,
        car_number: bookingForm.carNumber,
        safety_addon_opted: true,
        customer_notes: bookingForm.instructions
      });
      if (!active) {
        return;
      }
      dispatch(setQuote(response.data));
      setLoading(false);
    };

    if (!quote) {
      void load();
    } else {
      setLoading(false);
    }

    return () => {
      active = false;
    };
  }, [accessToken, bookingForm, dispatch, quote]);

  if (loading || !quote) {
    return (
      <Screen>
        <HeaderBlock eyebrow="Quote" title="Calculating fare." subtitle="Clear price before booking." visualVariant="trust" />
        <SectionCard>
          <Skeleton height={22} width="50%" />
          <View style={styles.stackSm}>
            <Skeleton height={18} width="80%" />
            <Skeleton height={18} width="90%" />
            <Skeleton height={18} width="70%" />
          </View>
        </SectionCard>
      </Screen>
    );
  }

  return (
    <Screen>
      <HeaderBlock eyebrow="Quote ready" title="Review fare." subtitle={`Expires ${formatCompactTime(quote.expires_at)}.`} visualVariant="trust" />
      <View style={styles.stackMd}>
        <QuoteSummaryStrip serviceType={quote.service_type} amount={formatCurrency(quote.fare_summary.amount_paise)} />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Service" value={displayServiceType(quote.service_type)} />
            <KeyValueRow label="Commercial model" value={quote.commercial_model ?? "Visible fare"} />
            <FareBreakdown
              items={quote.fare_components.map((component) => ({
                label: component.label,
                value: formatCurrency(component.amount_paise)
              }))}
              total={{ label: "Estimated total", value: formatCurrency(quote.fare_summary.amount_paise) }}
            />
          </View>
        </SectionCard>
        <StatusBanner tone="success" title="Bengaluru optimized pricing" message="Fare uses map route, pickup access, and current traffic signals internally." />
        <StatusBanner tone="info" title="Fair payout" message="Pickup access is included in the fare." />
        <StatusBanner tone="info" title="Cancellation summary" message={quote.cancellation_summary} />
        <BottomActionBar primaryLabel="Continue to review" secondaryLabel="Edit trip" onPrimaryPress={() => navigation.navigate("CustomerBookingReview")} onSecondaryPress={() => navigation.goBack()} primaryIcon="check" secondaryIcon="route" />
      </View>
    </Screen>
  );
}

export function CustomerBookingReviewScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);
  const quote = useAppSelector((state) => state.customer.currentQuote);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const dispatch = useAppDispatch();
  const [submitting, setSubmitting] = useState(false);

  const handleConfirm = async () => {
    if (!accessToken || !quote) {
      return;
    }

    setSubmitting(true);
    const response = await customerApi.createBooking(accessToken, {
      quote_id: quote.quote_id,
      service_type: toApiServiceType(bookingForm.serviceType),
      pickup_label: bookingForm.pickup,
      drop_label: bookingForm.drop,
      scheduled_pickup_at: bookingForm.scheduleAt,
      instructions: bookingForm.instructions
    });
    const deduplicated = bookings.filter((b) => b.booking_id !== response.data.booking_id);
    dispatch(setBookings([response.data, ...deduplicated]));
    dispatch(setActiveBooking(response.data.booking_id));
    navigation.navigate("CustomerBookingStatus");
    setSubmitting(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Review" title="Confirm details." subtitle="Check once before booking." visualVariant="trust" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Pickup" value={bookingForm.pickup} />
            <KeyValueRow label="Drop" value={bookingForm.drop} />
            <KeyValueRow label="Schedule" value={formatCompactTime(bookingForm.scheduleAt)} />
            <KeyValueRow label="Car" value={`${bookingForm.carBrandModel} (${bookingForm.transmissionType})`} />
            <KeyValueRow label="Quote total" value={formatCurrency(quote?.fare_summary.amount_paise ?? 0)} />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel={submitting ? "Creating booking..." : "Confirm booking"} secondaryLabel="Back" onPrimaryPress={handleConfirm} onSecondaryPress={() => navigation.goBack()} primaryDisabled={submitting} primaryIcon="check" secondaryIcon="arrowRight" />
      </View>
    </Screen>
  );
}

export function CustomerBookingStatusScreen() {
  const navigation = useNavigation<any>();
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const booking = bookings.find((item) => item.booking_id === activeBookingId) ?? bookings[0];

  if (!booking) {
    return (
      <Screen>
        <EmptyState title="No active booking found" message="Create a booking from a quote to enter the assignment flow." actionLabel="Go home" visualVariant="booking" onAction={() => navigation.navigate("CustomerHome")} />
      </Screen>
    );
  }

  return (
    <Screen>
      <HeaderBlock eyebrow="Booked" title="Finding driver." subtitle="Live assignment status." visualVariant="status" />
      <View style={styles.stackMd}>
        <StatusBanner tone="info" title="Pending assignment" message="We will update the driver status here." />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Booking ID" value={booking.booking_id} />
            <KeyValueRow label="Service" value={booking.service_type.replaceAll("_", " ")} />
            <KeyValueRow label="Pickup" value={booking.pickup_label} />
            <KeyValueRow label="Fare snapshot" value={formatCurrency(booking.fare_amount_paise)} />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel="View assigned driver" secondaryLabel="Booking detail" onPrimaryPress={() => navigation.navigate("CustomerAssignedDriver")} onSecondaryPress={() => navigation.navigate("CustomerBookingDetail")} primaryIcon="driver" secondaryIcon="ticket" />
      </View>
    </Screen>
  );
}

export function CustomerAssignedDriverScreen() {
  const navigation = useNavigation<any>();
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const booking = bookings.find((item) => item.booking_id === activeBookingId) ?? bookings[0];
  const pickupCoords = inferBengaluruCoords(booking?.pickup_label);
  const dropCoords = inferBengaluruCoords(booking?.drop_label);
  const pickupOtp = booking?.pickup_otp ?? "4821";
  const otpDigits = pickupOtp.split("");

  return (
    <Screen>
      <ScreenHeader title="Driver on the way" subtitle="Share this code to start the trip." />
      <View style={styles.stackMd}>
        <View style={styles.otpHero}>
          <AppText variant="caption" style={styles.otpHeroEyebrow}>Pickup OTP</AppText>
          <View style={styles.otpDigits}>
            {otpDigits.map((digit, index) => (
              <View key={`${digit}-${index}`} style={styles.otpDigitCell}>
                <AppText variant="section" style={styles.otpDigitText}>{digit}</AppText>
              </View>
            ))}
          </View>
          <AppText variant="caption" style={styles.otpHeroHint}>
            Tell your driver this code before the trip begins. Never share it in advance.
          </AppText>
        </View>
        <DriverTrustStrip />
        <RydvrseMapPreview
          pickupLabel={booking?.pickup_label ?? "Koramangala 4th Block"}
          dropLabel={booking?.drop_label ?? "Whitefield Main Road"}
          serviceType={toUiServiceType(booking?.service_type ?? "SCHEDULED_ONE_WAY")}
          distanceLabel={booking?.driver?.distance_km ? `${booking.driver.distance_km} km away` : "3.2 km away"}
          durationLabel={`${booking?.driver?.eta_minutes ?? 17} min`}
          pickupCoords={pickupCoords ?? undefined}
          dropCoords={dropCoords ?? undefined}
          interactive
        />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Driver" value={booking?.driver?.name ?? "Arun K"} />
            <KeyValueRow label="Rating" value={`${booking?.driver?.rating ?? 4.9}`} />
            <KeyValueRow label="Vehicle" value={booking?.driver?.vehicle_model ?? "Maruti Dzire - White"} />
            <KeyValueRow label="Plate" value={booking?.driver?.vehicle_plate ?? "KA 05 AB 1234"} />
            <KeyValueRow label="Languages" value={booking?.driver?.language ?? "English, Kannada"} />
            <KeyValueRow label="ETA" value={`${booking?.driver?.eta_minutes ?? 17} mins`} />
            <KeyValueRow label="Verification" value={booking?.driver?.verification_badge ?? "Verified + Trained"} />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel="Driver arrived" secondaryLabel="Need help" onPrimaryPress={() => navigation.navigate("CustomerStartTrip")} onSecondaryPress={() => navigation.navigate("CustomerSupport")} primaryIcon="check" secondaryIcon="help" />
      </View>
    </Screen>
  );
}

export function CustomerStartTripScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const [starting, setStarting] = useState(false);

  const handleStart = async () => {
    if (!accessToken || !activeBookingId) {
      return;
    }
    setStarting(true);
    await customerApi.confirmTripStart(accessToken, activeBookingId);
    navigation.navigate("CustomerActiveTrip");
    setStarting(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Start trip" title="Confirm handover." subtitle="Start only when ready." visualVariant="trust" />
      <View style={styles.stackMd}>
        <StatusBanner tone="success" title="Driver arrived" message="Add a note if needed." />
        <TextField label="Handover note" value="Fuel just below half tank. Please keep the charger in the center console." icon="document" multiline />
        <BottomActionBar primaryLabel={starting ? "Starting trip..." : "Confirm and start trip"} secondaryLabel="Pickup issue" onPrimaryPress={handleStart} onSecondaryPress={() => navigation.navigate("CustomerSupport")} primaryDisabled={starting} primaryIcon="check" secondaryIcon="alert" />
      </View>
    </Screen>
  );
}

export function CustomerActiveTripScreen() {
  const navigation = useNavigation<any>();
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);
  const { coords } = useCurrentLocation({ autoRequest: true, liveUpdates: true });

  const pickupCoords =
    bookingForm.pickupLatitude != null && bookingForm.pickupLongitude != null
      ? { latitude: bookingForm.pickupLatitude, longitude: bookingForm.pickupLongitude }
      : inferBengaluruCoords(bookingForm.pickup) ?? undefined;
  const dropCoords =
    bookingForm.dropLatitude != null && bookingForm.dropLongitude != null
      ? { latitude: bookingForm.dropLatitude, longitude: bookingForm.dropLongitude }
      : inferBengaluruCoords(bookingForm.drop) ?? undefined;
  const routeDistance = parsePositiveInt(bookingForm.distanceKm, 0);
  const routeMinutes = parsePositiveInt(bookingForm.predictedDriveMinutes, 0);

  return (
    <Screen>
      <HeaderBlock eyebrow="Trip" title="Live trip" subtitle="Track route and get help." visualVariant="customer" />
      <View style={styles.stackMd}>
        <TripMetricRow />
        <RydvrseMapPreview
          pickupLabel={bookingForm.pickup}
          dropLabel={bookingForm.drop}
          serviceType={bookingForm.serviceType}
          pickupCoords={pickupCoords}
          dropCoords={dropCoords}
          currentCoords={coords}
          distanceLabel={routeDistance ? `${routeDistance} km` : undefined}
          durationLabel={routeMinutes ? `${routeMinutes} min` : undefined}
          interactive
        />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Trip timer" value="00:24" />
            <KeyValueRow label="Route status" value="On track" />
            <KeyValueRow label="Driver" value="Arun K" />
          </View>
        </SectionCard>
        <StatusBanner tone="info" title="Need support?" message="Help includes ride context." />
        <BottomActionBar primaryLabel="Complete trip" secondaryLabel="Open support" onPrimaryPress={() => navigation.navigate("CustomerPayment")} onSecondaryPress={() => navigation.navigate("CustomerSupport")} primaryIcon="check" secondaryIcon="help" />
      </View>
    </Screen>
  );
}

export function CustomerPaymentScreen() {
  const navigation = useNavigation<any>();
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const booking = bookings.find((item) => item.booking_id === activeBookingId) ?? bookings[0];
  const [paying, setPaying] = useState(false);

  const handlePay = async () => {
    if (!accessToken || !activeBookingId) {
      return;
    }

    setPaying(true);
    await customerApi.createPaymentOrder(accessToken, activeBookingId);
    navigation.navigate("CustomerRatingIssue");
    setPaying(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Payment" title="Pay final fare." subtitle="Invoice and retry ready." visualVariant="status" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Final fare" value={formatCurrency(booking?.fare_amount_paise ?? 54900)} />
            <KeyValueRow label="Payment method" value="UPI" />
            <KeyValueRow label="Invoice status" value="Ready" />
          </View>
        </SectionCard>
        <StatusBanner tone="warning" title="Payment failed?" message="Retry without losing invoice." />
        <BottomActionBar primaryLabel={paying ? "Processing..." : "Pay now"} secondaryLabel="Fare issue" onPrimaryPress={handlePay} onSecondaryPress={() => navigation.navigate("CustomerSupport")} primaryDisabled={paying} primaryIcon="wallet" secondaryIcon="alert" />
      </View>
    </Screen>
  );
}

export function CustomerRatingIssueScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const [rating, setRating] = useState("5");
  const [issue, setIssue] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!accessToken || !activeBookingId) {
      return;
    }
    setSubmitting(true);
    await customerApi.submitRating(accessToken, activeBookingId, {
      rating: Number(rating),
      issue_reason: issue || null
    });
    navigation.navigate("CustomerHome");
    setSubmitting(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Rating" title="Rate the trip." subtitle="Add an issue if needed." visualVariant="trust" />
      <View style={styles.stackMd}>
        <TextField label="Rating out of 5" value={rating} onChangeText={setRating} keyboardType="numeric" icon="star" />
        <TextField label="Issue details (optional)" value={issue} onChangeText={setIssue} icon="alert" multiline />
        <BottomActionBar primaryLabel={submitting ? "Submitting..." : "Submit and return home"} secondaryLabel="Need support" onPrimaryPress={handleSubmit} onSecondaryPress={() => navigation.navigate("CustomerSupport")} primaryDisabled={submitting} primaryIcon="check" secondaryIcon="help" />
      </View>
    </Screen>
  );
}

export function CustomerBookingsScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(bookings.length === 0);

  useEffect(() => {
    let active = true;
    const load = async () => {
      if (!accessToken) {
        return;
      }
      const response = await customerApi.listBookings(accessToken);
      if (!active) {
        return;
      }
      dispatch(setBookings(response.data));
      setLoading(false);
    };

    if (bookings.length === 0) {
      void load();
    }

    return () => {
      active = false;
    };
  }, [accessToken, bookings.length, dispatch]);

  return (
    <Screen>
      <HeaderBlock eyebrow="Bookings" title="Your trips." subtitle="Upcoming and past rides." visualVariant="customer" />
      <View style={styles.stackMd}>
        {loading ? (
          <>
            <Skeleton height={88} radiusValue={24} />
            <Skeleton height={88} radiusValue={24} />
          </>
        ) : bookings.length > 0 ? (
          bookings.map((booking) => (
            <BookingCard
              key={booking.booking_id}
              title={displayServiceType(booking.service_type)}
              subtitle={`${booking.pickup_label} • ${formatCompactTime(booking.schedule_at)}`}
              amount={formatCurrency(booking.fare_amount_paise)}
              serviceType={booking.service_type}
              status={booking.status}
              onPress={() => {
                dispatch(setActiveBooking(booking.booking_id));
                navigation.navigate("CustomerBookingDetail");
              }}
            />
          ))
        ) : (
          <EmptyState title="Nothing booked yet" message="Your rides will appear here." visualVariant="booking" />
        )}
      </View>
    </Screen>
  );
}

export function CustomerBookingDetailScreen() {
  const navigation = useNavigation<any>();
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const booking = bookings.find((item) => item.booking_id === activeBookingId) ?? bookings[0];

  if (!booking) {
    return (
      <Screen>
        <EmptyState title="Booking unavailable" message="Open a trip from Bookings." visualVariant="booking" />
      </Screen>
    );
  }

  return (
    <Screen>
      <HeaderBlock eyebrow="Booking" title="Trip details." subtitle="Status and actions." visualVariant="trust" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Booking ID" value={booking.booking_id} />
            <KeyValueRow label="Status" value={booking.status} />
            <KeyValueRow label="Service" value={displayServiceType(booking.service_type)} />
            <KeyValueRow label="Pickup" value={booking.pickup_label} />
            <KeyValueRow label="Drop" value={booking.drop_label ?? "TBD"} />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel="Open support" secondaryLabel="Back to bookings" onPrimaryPress={() => navigation.navigate("CustomerSupport")} onSecondaryPress={() => navigation.goBack()} primaryIcon="help" secondaryIcon="ticket" />
      </View>
    </Screen>
  );
}

function FaqCard({ faq, onPress }: { faq: SupportFaq; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.faqCard, pressed && { opacity: 0.85 }]} accessibilityRole="button" accessibilityLabel={faq.question}>
      <View style={styles.faqIconWrap}>
        <AppIcon name="help" size={18} color={colors.brand.primary} secondaryColor={colors.brand.strong} />
      </View>
      <View style={styles.faqBody}>
        <AppText variant="caption" style={styles.faqCategory}>{faq.category}</AppText>
        <AppText variant="bodyStrong">{faq.question}</AppText>
      </View>
      <AppIcon name="arrowLeft" size={16} color={semantic.text.secondary} secondaryColor={semantic.text.secondary} />
    </Pressable>
  );
}

export function CustomerSupportScreen() {
  const navigation = useNavigation<any>();
  const [query, setQuery] = useState("");

  const trimmedQuery = query.trim();
  const filtered = useMemo(() => searchFaqs(trimmedQuery), [trimmedQuery]);
  const visibleFaqs = trimmedQuery.length > 0 ? filtered : SUPPORT_FAQS.slice(0, 3);

  const openChat = useCallback(
    (faq?: SupportFaq) => {
      navigation.navigate("CustomerSupportChat", {
        faqId: faq?.id ?? "custom",
        question: faq?.question ?? trimmedQuery,
      });
    },
    [navigation, trimmedQuery],
  );

  return (
    <Screen scrollable={false}>
      <ScreenHeader
        title="Help"
        subtitle="Quick answers and chat."
        onBack={() => navigation.navigate("CustomerHome")}
      />
      <ScrollView
        contentContainerStyle={styles.stackMd}
        showsVerticalScrollIndicator={false}
      >
        <TextField
          label="Search help"
          placeholder="Try 'fare', 'driver', or 'cancel'"
          value={query}
          onChangeText={setQuery}
          icon="help"
        />

        <View style={styles.supportQuickRow}>
          <Pressable style={styles.supportQuickCard} onPress={() => openChat()} accessibilityRole="button" accessibilityLabel="Chat with an agent">
            <View style={styles.supportQuickIcon}>
              <AppIcon name="help" size={18} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
            </View>
            <AppText variant="bodyStrong">Chat with us</AppText>
            <AppText variant="caption" style={styles.supportQuickHint}>Avg wait &lt; 2 min</AppText>
          </Pressable>
          <Pressable
            style={styles.supportQuickCardAlt}
            onPress={() => navigation.navigate("CustomerBookings")}
            accessibilityRole="button"
            accessibilityLabel="Open bookings"
          >
            <View style={styles.supportQuickIconAlt}>
              <AppIcon name="calendar" size={18} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
            </View>
            <AppText variant="bodyStrong">My trips</AppText>
            <AppText variant="caption" style={styles.supportQuickHint}>Report trip issue</AppText>
          </Pressable>
        </View>

        <View style={styles.sectionHeaderRow}>
          <AppText variant="section">
            {trimmedQuery ? `Matches for "${trimmedQuery}"` : "Popular questions"}
          </AppText>
          {trimmedQuery ? (
            <AppText variant="caption" style={styles.sectionHeaderMeta}>
              {filtered.length} result{filtered.length === 1 ? "" : "s"}
            </AppText>
          ) : null}
        </View>

        {visibleFaqs.length === 0 ? (
          <EmptyState
            title="No results"
            message="Start a chat and we'll help you directly."
            actionLabel="Chat with us"
            onAction={() => openChat()}
          />
        ) : (
          <View style={styles.stackSm}>
            {visibleFaqs.map((faq) => (
              <FaqCard key={faq.id} faq={faq} onPress={() => openChat(faq)} />
            ))}
          </View>
        )}
      </ScrollView>
    </Screen>
  );
}

export function CustomerProfileScreen() {
  const dispatch = useAppDispatch();
  const navigation = useNavigation<any>();
  const userName = useAppSelector((state) => state.session.userName);
  const mobile = useAppSelector((state) => state.session.mobileNumber);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const completedCount = bookings.filter((booking) => booking.status === "COMPLETED").length;
  const upcomingCount = bookings.filter((booking) => booking.status !== "COMPLETED").length;
  const fullName = userName ?? "Meera Singh";
  const initials = fullName
    .split(" ")
    .map((part) => part.charAt(0))
    .slice(0, 2)
    .join("")
    .toUpperCase();

  return (
    <Screen scrollable={false}>
      <ScreenHeader
        title="Profile"
        subtitle="Manage account"
        onBack={() => navigation.navigate("CustomerHome")}
        rightSlot={
          <Pressable
            onPress={() => navigation.navigate("CustomerProfileEdit")}
            accessibilityRole="button"
            accessibilityLabel="Edit profile"
            style={styles.profileEditButton}
            hitSlop={8}
          >
            <AppIcon name="document" size={16} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
          </Pressable>
        }
      />
      <ScrollView contentContainerStyle={styles.profileScroll} showsVerticalScrollIndicator={false}>
        <View style={styles.profileHero}>
          <View style={styles.profileAvatarOuter}>
            <View style={styles.profileAvatarInner}>
              <AppText variant="section" style={styles.profileAvatarText}>{initials || "M"}</AppText>
            </View>
            <Pressable
              onPress={() => navigation.navigate("CustomerProfileEdit")}
              accessibilityRole="button"
              accessibilityLabel="Edit profile details"
              style={styles.profilePencilBadge}
            >
              <AppIcon name="document" size={14} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
            </Pressable>
          </View>
          <AppText variant="section" style={styles.profileName}>{fullName}</AppText>
          <AppText variant="caption" style={styles.profileHandle}>{mobile ?? "+91 99999 99999"}</AppText>

          <View style={styles.profileStatsRow}>
            <View style={styles.profileStatCell}>
              <AppText variant="section">{bookings.length}</AppText>
              <AppText variant="caption">Trips</AppText>
            </View>
            <View style={styles.profileStatDivider} />
            <View style={styles.profileStatCell}>
              <AppText variant="section">{completedCount}</AppText>
              <AppText variant="caption">Completed</AppText>
            </View>
            <View style={styles.profileStatDivider} />
            <View style={styles.profileStatCell}>
              <AppText variant="section">{upcomingCount}</AppText>
              <AppText variant="caption">Upcoming</AppText>
            </View>
          </View>

          <Pressable
            style={styles.profileEditCta}
            onPress={() => navigation.navigate("CustomerProfileEdit")}
            accessibilityRole="button"
            accessibilityLabel="Edit profile"
          >
            <AppIcon name="document" size={15} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
            <AppText variant="bodyStrong" style={styles.profileEditCtaText}>Edit profile</AppText>
          </Pressable>
        </View>

        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Mobile" value={mobile ?? "+91 99999 99999"} />
            <KeyValueRow label="Email" value="meera@rydvrse.app" />
            <KeyValueRow label="City" value="Bengaluru" />
          </View>
        </SectionCard>

        <View style={styles.profileActionList}>
          <Pressable
            style={styles.profileActionRow}
            onPress={() => navigation.navigate("CustomerBookings")}
            accessibilityRole="button"
            accessibilityLabel="View past trips"
          >
            <View style={styles.profileActionIcon}>
              <AppIcon name="calendar" size={17} color={colors.brand.primary} secondaryColor={colors.brand.strong} />
            </View>
            <View style={styles.profileActionCopy}>
              <AppText variant="bodyStrong">Past trips</AppText>
            </View>
          </Pressable>
          <Pressable
            style={styles.profileActionRow}
            onPress={() => navigation.navigate("CustomerSupport")}
            accessibilityRole="button"
            accessibilityLabel="Get help"
          >
            <View style={styles.profileActionIcon}>
              <AppIcon name="help" size={17} color={colors.brand.primary} secondaryColor={colors.brand.strong} />
            </View>
            <View style={styles.profileActionCopy}>
              <AppText variant="bodyStrong">Help & support</AppText>
            </View>
          </Pressable>
          <Pressable
            style={styles.profileActionRow}
            onPress={() => dispatch(logout())}
            accessibilityRole="button"
            accessibilityLabel="Log out"
          >
            <View style={[styles.profileActionIcon, styles.profileActionIconDanger]}>
              <AppIcon name="logout" size={17} color={colors.brand.strong} secondaryColor={colors.brand.strong} />
            </View>
            <View style={styles.profileActionCopy}>
              <AppText variant="bodyStrong">Log out</AppText>
            </View>
          </Pressable>
        </View>
      </ScrollView>
    </Screen>
  );
}

export function CustomerProfileEditScreen() {
  const navigation = useNavigation<any>();
  const userName = useAppSelector((state) => state.session.userName);
  const mobile = useAppSelector((state) => state.session.mobileNumber);
  const [fullName, setFullName] = useState(userName ?? "Meera Singh");
  const [email, setEmail] = useState("meera@rydvrse.app");
  const [city, setCity] = useState("Bengaluru");
  const [error, setError] = useState("");

  const handleContinue = () => {
    if (!fullName.trim() || !email.trim()) {
      setError("Name and email are required.");
      return;
    }
    setError("");
    navigation.navigate("CustomerProfileOtp", {
      full_name: fullName,
      email,
      city_name: city,
    });
  };

  return (
    <Screen scrollable={false}>
      <ScreenHeader title="Edit profile" subtitle="We'll verify changes with an OTP." />
      <ScrollView contentContainerStyle={styles.stackSm} showsVerticalScrollIndicator={false}>
        <SectionCard>
          <View style={styles.stackSm}>
            <TextField label="Full name" value={fullName} onChangeText={setFullName} icon="profile" />
            <TextField label="Email" value={email} onChangeText={setEmail} icon="document" />
            <TextField label="City" value={city} onChangeText={setCity} icon="city" />
            <KeyValueRow label="Mobile" value={mobile ?? "+91 99999 99999"} />
            <AppText variant="caption">Mobile changes require a full re-verification. Contact support if you need to update it.</AppText>
          </View>
        </SectionCard>
        {error ? <StatusBanner tone="warning" title="Incomplete" message={error} /> : null}
        <BottomActionBar
          primaryLabel="Verify with OTP"
          secondaryLabel="Cancel"
          onPrimaryPress={handleContinue}
          onSecondaryPress={() => navigation.goBack()}
          primaryIcon="check"
          secondaryIcon="logout"
        />
      </ScrollView>
    </Screen>
  );
}

export function CustomerProfileOtpScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const mobile = useAppSelector((state) => state.session.mobileNumber);
  const [otp, setOtp] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [saved, setSaved] = useState(false);
  const payload = route.params ?? {};

  const handleVerify = async () => {
    if (otp.trim().length < 4) {
      setError("Enter the 4-digit code we sent to your mobile.");
      return;
    }
    if (!accessToken) {
      setError("Session expired. Please log in again.");
      return;
    }
    setError("");
    setSaving(true);
    try {
      await customerApi.updateProfile(accessToken, {
        full_name: payload.full_name,
        email: payload.email,
        city_name: payload.city_name,
      });
      setSaved(true);
      setTimeout(() => {
        navigation.navigate("CustomerTabs", { screen: "CustomerProfile" });
      }, 800);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Screen scrollable={false}>
      <ScreenHeader title="Verify changes" subtitle={`We sent a 4-digit code to ${mobile ?? "your mobile"}.`} />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <TextField label="OTP" placeholder="Enter 4-digit code" value={otp} onChangeText={setOtp} icon="shield" keyboardType="numeric" />
            <AppText variant="caption">For test mode the code is 4821. In production, the code arrives via SMS.</AppText>
          </View>
        </SectionCard>
        {error ? <StatusBanner tone="warning" title="Not verified" message={error} /> : null}
        {saved ? <StatusBanner tone="success" title="Profile updated" message="Your changes are live." /> : null}
        <BottomActionBar
          primaryLabel={saving ? "Verifying..." : "Verify and save"}
          secondaryLabel="Back"
          onPrimaryPress={handleVerify}
          onSecondaryPress={() => navigation.goBack()}
          primaryDisabled={saving}
          primaryIcon="check"
          secondaryIcon="logout"
        />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
  homeCanvas: {
    flex: 1,
    backgroundColor: semantic.bg.app,
  },
  homeCanvasWide: {
    flexDirection: "row",
    alignItems: "stretch",
    padding: spacing.lg,
    gap: spacing.lg,
  },
  homeMapPanel: {
    overflow: "hidden",
    backgroundColor: colors.brand.subtle,
  },
  homeMapPanelWide: {
    flex: 1.2,
    borderRadius: 32,
  },
  homeSheetPanel: {
    marginTop: -24,
  },
  homeSheetPanelWide: {
    flex: 0.8,
    marginTop: 0,
    alignSelf: "center",
  },
  detailsSheet: {
    backgroundColor: semantic.bg.surface,
    borderRadius: 28,
    padding: spacing.md,
    gap: spacing.sm,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  compactHeader: {
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
  compactHeaderCopy: {
    flex: 1,
    gap: 2,
  },
  tripTypeGrid: {
    flexDirection: "row",
    gap: spacing.sm,
  },
  optionGrid: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm,
  },
  bookingRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: spacing.md,
    flexWrap: "wrap"
  },
  bookingMain: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
    flex: 1,
    minWidth: 240
  },
  bookingIconWrap: {
    width: 52,
    height: 52,
    borderRadius: 18,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center"
  },
  bookingContent: {
    flex: 1,
    gap: spacing.xs
  },
  bookingHeader: {
    gap: spacing.xs
  },
  bookingAside: {
    minWidth: 110,
    alignItems: "flex-end",
    gap: spacing.xs
  },
  bookingCaption: {
    color: colors.brand.primary
  },
  signalStrip: {
    gap: spacing.sm
  },
  signalCard: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    paddingVertical: spacing.xs
  },
  signalIconWrap: {
    width: 40,
    height: 40,
    borderRadius: 14,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center"
  },
  signalText: {
    flex: 1,
    gap: 2
  },
  metricStrip: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.sm
  },
  metricCard: {
    flex: 1,
    minWidth: 140,
    backgroundColor: semantic.bg.surface,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    padding: spacing.md,
    gap: spacing.xs
  },
  metricIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 14,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center"
  },
  stackSm: {
    gap: spacing.sm
  },
  stackMd: {
    gap: spacing.md
  },
  stackLg: {
    gap: spacing.lg,
    marginTop: spacing.xxl
  },
  rowBetween: {
    flexDirection: "row",
    justifyContent: "space-between",
    gap: spacing.md
  },
  // Support screen
  supportQuickRow: {
    flexDirection: "row",
    gap: spacing.sm,
  },
  supportQuickCard: {
    flex: 1,
    backgroundColor: colors.brand.primary,
    borderRadius: 20,
    padding: spacing.md,
    gap: spacing.xs,
  },
  supportQuickCardAlt: {
    flex: 1,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    borderRadius: 20,
    padding: spacing.md,
    gap: spacing.xs,
  },
  supportQuickIcon: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: colors.brand.strong,
    alignItems: "center",
    justifyContent: "center",
  },
  supportQuickIconAlt: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  supportQuickHint: {
    color: semantic.text.secondary,
  },
  sectionHeaderRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  sectionHeaderMeta: {
    color: semantic.text.secondary,
  },
  faqCard: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: 18,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  faqIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  faqBody: {
    flex: 1,
    gap: 2,
  },
  faqCategory: {
    color: colors.brand.strong,
    textTransform: "uppercase",
    letterSpacing: 0.6,
  },
  viewMoreButton: {
    alignSelf: "center",
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.lg,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    backgroundColor: semantic.bg.surface,
  },
  viewMoreText: {
    color: semantic.text.primary,
  },
  supportContactRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
  },
  supportContactIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  supportContactCopy: {
    flex: 1,
    gap: 2,
  },
  supportContactCta: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs,
    borderRadius: 999,
    backgroundColor: colors.brand.primary,
  },
  supportContactCtaText: {
    color: semantic.text.onBrand,
  },
  // Profile screen
  profileScroll: {
    gap: spacing.md,
    paddingBottom: spacing.lg,
  },
  profileEditButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  profileHero: {
    alignItems: "center",
    gap: spacing.sm,
    paddingVertical: spacing.lg,
    backgroundColor: semantic.bg.surface,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  profileAvatarOuter: {
    width: 112,
    height: 112,
    borderRadius: 56,
    padding: 4,
    backgroundColor: colors.brand.primary,
    alignItems: "center",
    justifyContent: "center",
    position: "relative",
  },
  profileAvatarInner: {
    width: 104,
    height: 104,
    borderRadius: 52,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  profileAvatarText: {
    fontSize: 32,
    color: semantic.text.primary,
  },
  profilePencilBadge: {
    position: "absolute",
    right: -2,
    bottom: -2,
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: colors.brand.strong,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 3,
    borderColor: semantic.bg.surface,
  },
  profileName: {
    fontSize: 20,
    textAlign: "center",
  },
  profileHandle: {
    color: semantic.text.secondary,
    textAlign: "center",
  },
  profileStatsRow: {
    marginTop: spacing.sm,
    flexDirection: "row",
    alignItems: "center",
    alignSelf: "stretch",
    marginHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    borderColor: semantic.border.soft,
  },
  profileStatCell: {
    flex: 1,
    alignItems: "center",
    gap: 2,
  },
  profileStatDivider: {
    width: 1,
    alignSelf: "stretch",
    backgroundColor: semantic.border.soft,
  },
  profileEditCta: {
    marginTop: spacing.sm,
    flexDirection: "row",
    alignItems: "center",
    gap: 6,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
    borderRadius: 999,
    backgroundColor: colors.brand.soft,
    borderWidth: 1,
    borderColor: colors.brand.primary,
  },
  profileEditCtaText: {
    color: semantic.text.primary,
  },
  profileActionList: {
    gap: spacing.sm,
  },
  profileActionRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: 18,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  profileActionIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  profileActionIconDanger: {
    backgroundColor: "#FEE2E2",
  },
  profileActionCopy: {
    flex: 1,
    gap: 2,
  },
  // Pickup OTP hero
  otpHero: {
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.lg,
    borderRadius: 24,
    backgroundColor: colors.brand.soft,
    borderWidth: 1,
    borderColor: colors.brand.primary,
  },
  otpHeroEyebrow: {
    color: colors.brand.strong,
    textTransform: "uppercase",
    letterSpacing: 1.2,
  },
  otpDigits: {
    flexDirection: "row",
    gap: spacing.sm,
  },
  otpDigitCell: {
    width: 56,
    height: 64,
    borderRadius: 16,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: colors.brand.primary,
  },
  otpDigitText: {
    fontSize: 28,
  },
  otpHeroHint: {
    color: semantic.text.secondary,
    textAlign: "center",
    paddingHorizontal: spacing.md,
  },
});
