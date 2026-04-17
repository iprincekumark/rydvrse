import React, { useEffect, useState } from "react";
import { Pressable, StyleSheet, View, useWindowDimensions } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { ServiceIcon } from "@/assets/icons/ServiceIcon";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { BookingHomeSheet } from "@/components/booking/BookingHomeSheet";
import { HeaderBlock } from "@/components/layout/HeaderBlock";
import { Screen } from "@/components/layout/Screen";
import { AppText } from "@/components/common/AppText";
import { TextField } from "@/components/forms/TextField";
import { SectionCard } from "@/components/cards/SectionCard";
import { ChoiceCard } from "@/components/common/ChoiceCard";
import { StatusBanner } from "@/components/common/StatusBanner";
import { StatusChip } from "@/components/common/StatusChip";
import { KeyValueRow } from "@/components/common/KeyValueRow";
import { MapPlaceholderCard } from "@/components/cards/MapPlaceholderCard";
import { EmptyState } from "@/components/common/EmptyState";
import { Skeleton } from "@/components/loaders/Skeleton";
import { FareBreakdown } from "@/components/patterns/FareBreakdown";
import { RydvrseMapPreview } from "@/components/maps/RydvrseMapPreview";
import { supportCategories } from "@/constants/support";
import { CustomerServiceType, serviceTypeOptions } from "@/constants/serviceTypes";
import { authApi } from "@/services/api/auth";
import { customerApi } from "@/services/api/customer";
import { useAppDispatch, useAppSelector } from "@/store";
import { markProfileComplete, setActiveBooking, setBookings, setQuote, updateBookingForm } from "@/store/customerSlice";
import { hydrateSession, logout } from "@/store/sessionSlice";
import { colors, semantic, spacing } from "@/theme";
import { formatCompactTime, formatCurrency } from "@/utils/format";

const serviceEyebrowMap: Record<string, string> = {
  ONE_WAY_DROP: "Direct",
  ROUND_TRIP: "Return"
};

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

function CompactHeader({
  title,
  subtitle,
  onBack,
}: {
  title: string;
  subtitle?: string;
  onBack?: () => void;
}) {
  return (
    <View style={styles.compactHeader}>
      {onBack ? (
        <Pressable onPress={onBack} style={styles.backButton} accessibilityRole="button" accessibilityLabel="Go back">
          <AppIcon name="arrowLeft" size={20} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
        </Pressable>
      ) : null}
      <View style={styles.compactHeaderCopy}>
        <AppText variant="section">{title}</AppText>
        {subtitle ? <AppText variant="caption">{subtitle}</AppText> : null}
      </View>
    </View>
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
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(true);
  const isWide = width >= 720;
  const mapHeight = isWide ? Math.max(520, height - 120) : Math.max(230, Math.min(310, height * 0.36));

  useEffect(() => {
    let active = true;
    const load = async () => {
      if (!accessToken) {
        return;
      }

      const response = await customerApi.home(accessToken);
      if (!active) {
        return;
      }
      dispatch(setBookings(response.data.upcoming_bookings ?? []));
      setLoading(false);
    };
    void load();
    return () => {
      active = false;
    };
  }, [accessToken, dispatch]);

  const nextBooking = bookings[0];

  return (
    <Screen padded={false} scrollable={false} variant="map" backgroundColor={semantic.bg.app}>
      <View style={[styles.homeCanvas, isWide && styles.homeCanvasWide]}>
        <View style={[styles.homeMapPanel, { height: mapHeight }, isWide && styles.homeMapPanelWide]}>
          <RydvrseMapPreview
            pickupLabel={bookingForm.pickup}
            dropLabel={bookingForm.drop}
            distanceKm={bookingForm.distanceKm}
            etaMinutes={bookingForm.predictedDriveMinutes}
            onUseCurrentLocation={() => dispatch(updateBookingForm({ pickup: "Current location • Bengaluru" }))}
            onOpenSearch={() => navigation.navigate("CustomerServiceSetup")}
            onRecenter={() => dispatch(updateBookingForm({ pickup: bookingForm.pickup || "Koramangala 4th Block" }))}
          />
        </View>

        <View style={[styles.homeSheetPanel, isWide && styles.homeSheetPanelWide]}>
          <BookingHomeSheet
            serviceType={bookingForm.serviceType}
            pickup={bookingForm.pickup}
            drop={bookingForm.drop}
            scheduleLabel={formatCompactTime(bookingForm.scheduleAt)}
            distanceKm={bookingForm.distanceKm}
            etaMinutes={bookingForm.predictedDriveMinutes}
            onTripTypeChange={(serviceType) => dispatch(updateBookingForm({ serviceType }))}
            onQuickSchedule={(kind) => dispatch(updateBookingForm({ scheduleAt: scheduleIso(kind) }))}
            onOpenLocationSearch={() => navigation.navigate("CustomerServiceSetup")}
            onOpenDetails={() => navigation.navigate("CustomerServiceSetup")}
            onGetFare={() => navigation.navigate("CustomerQuote")}
          />

          {loading || nextBooking ? (
            <View style={styles.homeBelowSheet}>
              {loading ? (
                <SectionCard>
                  <Skeleton height={18} width="45%" />
                  <View style={styles.stackSm}>
                    <Skeleton height={46} />
                    <Skeleton height={46} width="82%" />
                  </View>
                </SectionCard>
              ) : nextBooking ? (
                <View style={styles.stackSm}>
                  <SectionTitle label="Upcoming booking" />
                  <BookingCard
                    title={displayServiceType(nextBooking.service_type)}
                    subtitle={`${nextBooking.pickup_label} • ${formatCompactTime(nextBooking.schedule_at)}`}
                    amount={formatCurrency(nextBooking.fare_amount_paise)}
                    serviceType={nextBooking.service_type}
                    status={nextBooking.status}
                    onPress={() => {
                      dispatch(setActiveBooking(nextBooking.booking_id));
                      navigation.navigate("CustomerBookingDetail");
                    }}
                  />
                </View>
              ) : null}
            </View>
          ) : null}
        </View>
      </View>
    </Screen>
  );
}

export function CustomerServiceSetupScreen() {
  const navigation = useNavigation<any>();
  const dispatch = useAppDispatch();
  const bookingForm = useAppSelector((state) => state.customer.bookingForm);

  return (
    <Screen>
      <CompactHeader title="Trip details" subtitle="Edit only what matters." onBack={() => navigation.goBack()} />
      <View style={styles.stackSm}>
        <View style={styles.tripTypeGrid}>
          {serviceTypeOptions.map((option) => (
            <ChoiceCard
              key={option.id}
              title={option.title}
              subtitle={option.subtitle}
              eyebrow={serviceEyebrowMap[option.id]}
              leading={<ServiceIcon serviceType={option.id} />}
              selected={bookingForm.serviceType === option.id}
              onPress={() => dispatch(updateBookingForm({ serviceType: option.id }))}
            />
          ))}
        </View>
        <TextField label="Pickup location" value={bookingForm.pickup} onChangeText={(value) => dispatch(updateBookingForm({ pickup: value }))} icon="pin" />
        <TextField label="Drop location" value={bookingForm.drop} onChangeText={(value) => dispatch(updateBookingForm({ drop: value }))} icon="route" />
        <TextField label="Scheduled time" value={bookingForm.scheduleAt} onChangeText={(value) => dispatch(updateBookingForm({ scheduleAt: value }))} icon="calendar" />
        <TextField label="Duration" value={bookingForm.durationLabel} onChangeText={(value) => dispatch(updateBookingForm({ durationLabel: value }))} icon="clock" />
        <SectionCard>
          <View style={styles.stackSm}>
            <SectionTitle label="Route" />
            <TextField label="Distance km" value={bookingForm.distanceKm} onChangeText={(value) => dispatch(updateBookingForm({ distanceKm: value }))} icon="route" keyboardType="numeric" />
            <TextField label="Traffic ETA min" value={bookingForm.predictedDriveMinutes} onChangeText={(value) => dispatch(updateBookingForm({ predictedDriveMinutes: value }))} icon="clock" keyboardType="numeric" />
            <TextField label="Driver pickup distance km" value={bookingForm.driverPickupDistanceKm} onChangeText={(value) => dispatch(updateBookingForm({ driverPickupDistanceKm: value }))} icon="pin" keyboardType="numeric" />
            <TextField label="Driver pickup ETA min" value={bookingForm.driverPickupEtaMinutes} onChangeText={(value) => dispatch(updateBookingForm({ driverPickupEtaMinutes: value }))} icon="eta" keyboardType="numeric" />
          </View>
        </SectionCard>
        <SectionCard>
          <View style={styles.stackSm}>
            <SectionTitle label="Car details" />
            <TextField label="Transmission" value={bookingForm.transmissionType} onChangeText={(value) => dispatch(updateBookingForm({ transmissionType: value.toUpperCase() }))} icon="car" />
            <TextField label="Car type" value={bookingForm.carType} onChangeText={(value) => dispatch(updateBookingForm({ carType: value.toUpperCase() }))} icon="car" />
            <TextField label="Brand and model" value={bookingForm.carBrandModel} onChangeText={(value) => dispatch(updateBookingForm({ carBrandModel: value }))} icon="document" />
            <TextField label="Car number" value={bookingForm.carNumber} onChangeText={(value) => dispatch(updateBookingForm({ carNumber: value.toUpperCase() }))} icon="ticket" />
          </View>
        </SectionCard>
        <TextField label="Notes" value={bookingForm.instructions} onChangeText={(value) => dispatch(updateBookingForm({ instructions: value }))} icon="document" multiline />
        <BottomActionBar primaryLabel="Get fare" secondaryLabel="Back" onPrimaryPress={() => navigation.navigate("CustomerQuote")} onSecondaryPress={() => navigation.goBack()} primaryIcon="spark" secondaryIcon="arrowLeft" />
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
      const response = await customerApi.quote(accessToken, {
        service_type: toApiServiceType(bookingForm.serviceType),
        pickup: {
          label: bookingForm.pickup,
          address_line_1: bookingForm.pickup,
          city_id: BENGALURU_CITY_ID,
          latitude: 12.9352,
          longitude: 77.6245
        },
        drop: {
          label: bookingForm.drop,
          address_line_1: bookingForm.drop,
          city_id: BENGALURU_CITY_ID,
          latitude: 12.9698,
          longitude: 77.75
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
        {quote.pricing_assumptions ? (
          <View style={styles.metricStrip}>
            <View style={styles.metricCard}>
              <View style={styles.metricIconWrap}>
                <AppIcon name="route" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
              </View>
              <AppText variant="caption">Distance</AppText>
              <AppText variant="bodyStrong">{quote.pricing_assumptions.rounded_distance_km ?? "--"} km</AppText>
            </View>
            <View style={styles.metricCard}>
              <View style={styles.metricIconWrap}>
                <AppIcon name="clock" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
              </View>
              <AppText variant="caption">Traffic ETA</AppText>
              <AppText variant="bodyStrong">{quote.pricing_assumptions.predicted_drive_minutes ?? "--"} min</AppText>
            </View>
            <View style={styles.metricCard}>
              <View style={styles.metricIconWrap}>
                <AppIcon name="eta" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
              </View>
              <AppText variant="caption">Driver arrival</AppText>
              <AppText variant="bodyStrong">{quote.pricing_assumptions.driver_pickup_eta_minutes ?? "--"} min</AppText>
            </View>
          </View>
        ) : null}
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
        <StatusBanner tone="success" title="Bengaluru optimized pricing" message={quote.savings_summary?.message ?? quote.assignment_note} />
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
            <KeyValueRow label="Duration" value={bookingForm.durationLabel} />
            <KeyValueRow label="Distance and traffic ETA" value={`${bookingForm.distanceKm} km / ${bookingForm.predictedDriveMinutes} min`} />
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

  return (
    <Screen>
      <HeaderBlock eyebrow="Driver" title="Driver assigned." subtitle="ETA and identity." visualVariant="customer" />
      <View style={styles.stackMd}>
        <DriverTrustStrip />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Driver" value={booking?.driver?.name ?? "Arun K"} />
            <KeyValueRow label="Rating" value={String(booking?.driver?.rating ?? 4.9)} />
            <KeyValueRow label="Languages" value={booking?.driver?.language ?? "English, Kannada"} />
            <KeyValueRow label="ETA" value={`${booking?.driver?.eta_minutes ?? 17} mins`} />
            <KeyValueRow label="Verification" value={booking?.driver?.verification_badge ?? "Verified + Trained"} />
          </View>
        </SectionCard>
        <MapPlaceholderCard title="Driver approach" subtitle="Driver location and ETA appear here." />
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

  return (
    <Screen>
      <HeaderBlock eyebrow="Trip" title="Live trip." subtitle="Track and get help." visualVariant="customer" />
      <View style={styles.stackMd}>
        <TripMetricRow />
        <MapPlaceholderCard />
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

export function CustomerSupportScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const [category, setCategory] = useState(supportCategories[0]);
  const [description, setDescription] = useState("Driver delayed.");
  const [success, setSuccess] = useState("");

  const handleSubmit = async () => {
    if (!accessToken) {
      return;
    }
    await customerApi.createSupportTicket(accessToken, {
      category,
      description,
      booking_id: activeBookingId
    });
    setSuccess("Ticket created.");
  };

  return (
    <Screen scrollable={false}>
      <CompactHeader title="Help" subtitle="Short issue, quick action." onBack={() => navigation.goBack()} />
      <View style={styles.stackSm}>
        <TextField label="Category" value={category} onChangeText={setCategory} icon="help" />
        <TextField label="Issue" value={description} onChangeText={setDescription} icon="document" multiline />
        {success ? <StatusBanner tone="success" title="Submitted" message={success} /> : null}
        <BottomActionBar primaryLabel="Submit ticket" onPrimaryPress={handleSubmit} primaryIcon="check" />
      </View>
    </Screen>
  );
}

export function CustomerProfileScreen() {
  const dispatch = useAppDispatch();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const userName = useAppSelector((state) => state.session.userName);
  const mobile = useAppSelector((state) => state.session.mobileNumber);
  const [fullName, setFullName] = useState(userName ?? "Meera Singh");
  const [email, setEmail] = useState("meera@rydvrse.app");
  const [city, setCity] = useState("Bengaluru");
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState("");

  const handleSave = async () => {
    if (!accessToken || saving) {
      return;
    }

    try {
      setSaving(true);
      setSaved("");
      await customerApi.updateProfile(accessToken, {
        full_name: fullName,
        email,
        city_name: city,
      });
      setSaved("Profile updated.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Screen scrollable={false}>
      <CompactHeader title="Profile" subtitle="Edit account details." />
      <View style={styles.stackSm}>
        <TextField label="Name" value={fullName} onChangeText={setFullName} icon="profile" />
        <TextField label="Email" value={email} onChangeText={setEmail} icon="document" />
        <TextField label="City" value={city} onChangeText={setCity} icon="city" />
        <SectionCard>
          <KeyValueRow label="Mobile" value={mobile ?? "+919999999999"} />
        </SectionCard>
        {saved ? <StatusBanner tone="success" title="Saved" message={saved} /> : null}
        <BottomActionBar primaryLabel={saving ? "Saving..." : "Save"} secondaryLabel="Log out" onPrimaryPress={handleSave} onSecondaryPress={() => dispatch(logout())} primaryDisabled={saving || !fullName.trim()} primaryIcon="check" secondaryIcon="logout" />
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
  homeBelowSheet: {
    paddingHorizontal: spacing.md,
    paddingTop: spacing.sm,
    paddingBottom: spacing.md,
    gap: spacing.sm,
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
  }
});
