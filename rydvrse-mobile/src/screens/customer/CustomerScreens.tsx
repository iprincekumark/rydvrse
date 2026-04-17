import React, { useEffect, useState } from "react";
import { Pressable, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { ServiceIcon } from "@/assets/icons/ServiceIcon";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
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
import { supportCategories } from "@/constants/support";
import { CustomerServiceType, serviceTypeOptions } from "@/constants/serviceTypes";
import { authApi } from "@/services/api/auth";
import { customerApi } from "@/services/api/customer";
import { useAppDispatch, useAppSelector } from "@/store";
import { markProfileComplete, setActiveBooking, setBookings, setQuote, updateBookingForm } from "@/store/customerSlice";
import { hydrateSession, logout } from "@/store/sessionSlice";
import { colors, spacing } from "@/theme";
import { formatCompactTime, formatCurrency } from "@/utils/format";

const serviceEyebrowMap: Record<string, string> = {
  SCHEDULED_LOCAL: "Plan ahead",
  ONE_WAY_DROP: "Clear return cost",
  ROUND_TRIP: "Same driver, longer flow",
  AIRPORT: "Flat zone fare",
  LATE_NIGHT_SAFE_RETURN: "Trust-first night flow"
};

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
              <ServiceIcon serviceType={serviceType as CustomerServiceType} />
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
              <AppIcon name={item.icon} size={18} color={colors.primary.base} secondaryColor={colors.secondary.muted} />
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
          <ServiceIcon serviceType={serviceType as CustomerServiceType} />
        </View>
        <AppText variant="caption">Service</AppText>
        <AppText variant="bodyStrong">{serviceType.replaceAll("_", " ")}</AppText>
      </View>
      <View style={styles.metricCard}>
        <View style={styles.metricIconWrap}>
          <AppIcon name="wallet" size={18} color={colors.primary.base} secondaryColor={colors.secondary.muted} />
        </View>
        <AppText variant="caption">Estimated total</AppText>
        <AppText variant="bodyStrong">{amount}</AppText>
      </View>
      <View style={styles.metricCard}>
        <View style={styles.metricIconWrap}>
          <AppIcon name="shield" size={18} color={colors.primary.base} secondaryColor={colors.secondary.muted} />
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
            <AppIcon name={item.icon} size={18} color={colors.primary.base} secondaryColor={colors.secondary.muted} />
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
            <AppIcon name={item.icon} size={18} color={colors.primary.base} secondaryColor={colors.secondary.muted} />
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
      <HeaderBlock eyebrow="Customer App" title="Book a verified driver with clarity from the first tap." subtitle="Sign in with your mobile number to start a scheduled-first booking flow." visualVariant="customer" />
      <View style={styles.stackLg}>
        <TextField label="Mobile number" value={phone} onChangeText={setPhone} keyboardType="phone-pad" icon="phone" helperText={error || "OTP login keeps the experience fast and low-friction."} />
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
        <TextField label="OTP code" value={otp} onChangeText={setOtp} keyboardType="numeric" icon="ticket" helperText={error || "Use 123456 in mock mode for local flow testing."} />
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
    // Navigation happens automatically via conditional rendering in RootNavigator
    // when profileComplete becomes true — no manual navigate needed.
    setSaving(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="First-time setup" title="Tell us who’s booking and which city you’ll start from." subtitle="This keeps serviceability, pricing, and support context accurate from your very first ride." visualVariant="trust" />
      <View style={styles.stackMd}>
        <TextField label="Full name" value={fullName} onChangeText={setFullName} icon="profile" />
        <TextField label="Email (optional)" value={email} onChangeText={setEmail} icon="document" />
        <TextField label="Launch city" value={city} onChangeText={setCity} icon="city" />
        <StatusBanner tone="info" title="Manual address entry stays available." message="Location permission helps with faster quote setup, but it is never required to proceed." />
        <BottomActionBar primaryLabel={saving ? "Saving..." : "Continue"} onPrimaryPress={handleSave} primaryDisabled={saving || !fullName.trim()} primaryIcon="check" />
      </View>
    </Screen>
  );
}

export function CustomerHomeScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const userName = useAppSelector((state) => state.session.userName);
  const bookings = useAppSelector((state) => state.customer.bookings);
  const dispatch = useAppDispatch();
  const [loading, setLoading] = useState(true);

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
    <Screen>
      <HeaderBlock eyebrow="Customer home" title={`Good evening, ${userName?.split(" ")[0] ?? "there"}.`} subtitle="Scheduled-first booking keeps pricing clearer and fulfillment calmer." visualVariant="customer" />
      <View style={styles.stackLg}>
        {loading ? (
          <SectionCard>
            <Skeleton height={22} width="55%" />
            <View style={styles.stackSm}>
              <Skeleton height={64} />
              <Skeleton height={64} />
            </View>
          </SectionCard>
        ) : null}

        {!loading ? (
          <>
            <CustomerSignalStrip />
            <View style={styles.stackSm}>
              <SectionTitle label="Book a driver" />
              {serviceTypeOptions.map((service) => (
                <ChoiceCard
                  key={service.id}
                  title={service.title}
                  subtitle={service.subtitle}
                  eyebrow={serviceEyebrowMap[service.id]}
                  leading={<ServiceIcon serviceType={service.id} />}
                  onPress={() => {
                    dispatch(updateBookingForm({ serviceType: service.id }));
                    navigation.navigate("CustomerServiceSetup");
                  }}
                />
              ))}
            </View>

            {nextBooking ? (
              <View style={styles.stackSm}>
                <SectionTitle label="Upcoming booking" />
                <BookingCard
                  title={nextBooking.service_type.replaceAll("_", " ")}
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
            ) : (
              <EmptyState title="No upcoming bookings yet" message="Once you confirm a quote, your active and upcoming rides will stay visible here." actionLabel="Open booking history" visualVariant="booking" onAction={() => navigation.navigate("CustomerBookings")} />
            )}
          </>
        ) : null}
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
      <HeaderBlock eyebrow="Service setup" title="Shape the booking before we price it." subtitle="Rydvrse shows the fare components before you confirm, so this screen is where we gather the exact trip context." visualVariant="customer" />
      <View style={styles.stackMd}>
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
        <TextField label="Pickup location" value={bookingForm.pickup} onChangeText={(value) => dispatch(updateBookingForm({ pickup: value }))} icon="pin" />
        <TextField label="Drop location" value={bookingForm.drop} onChangeText={(value) => dispatch(updateBookingForm({ drop: value }))} icon="route" />
        <TextField label="Scheduled time" value={bookingForm.scheduleAt} onChangeText={(value) => dispatch(updateBookingForm({ scheduleAt: value }))} icon="calendar" />
        <TextField label="Expected duration" value={bookingForm.durationLabel} onChangeText={(value) => dispatch(updateBookingForm({ durationLabel: value }))} icon="clock" />
        <TextField label="Special instructions" value={bookingForm.instructions} onChangeText={(value) => dispatch(updateBookingForm({ instructions: value }))} icon="document" multiline />
        <BottomActionBar primaryLabel="Get transparent quote" secondaryLabel="Back" onPrimaryPress={() => navigation.navigate("CustomerQuote")} onSecondaryPress={() => navigation.goBack()} primaryIcon="spark" secondaryIcon="arrowRight" />
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
      const response = await customerApi.quote(accessToken, {
        service_type: bookingForm.serviceType,
        pickup_label: bookingForm.pickup,
        drop_label: bookingForm.drop,
        schedule_at: bookingForm.scheduleAt,
        duration_label: bookingForm.durationLabel
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
        <HeaderBlock eyebrow="Quote" title="We’re calculating the fare with every visible component." subtitle="No hidden post-trip pricing. You’ll see the full breakdown before you confirm." visualVariant="trust" />
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
      <HeaderBlock eyebrow="Quote ready" title="This price is built to be clear before the ride begins." subtitle={`Quote expires at ${formatCompactTime(quote.expires_at)}.`} visualVariant="trust" />
      <View style={styles.stackMd}>
        <QuoteSummaryStrip serviceType={quote.service_type} amount={formatCurrency(quote.fare_summary.amount_paise)} />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Service" value={quote.service_type.replaceAll("_", " ")} />
            <KeyValueRow label="Estimated total" value={formatCurrency(quote.fare_summary.amount_paise)} />
            {quote.fare_components.map((component) => (
              <KeyValueRow key={component.code} label={component.label} value={formatCurrency(component.amount_paise)} />
            ))}
          </View>
        </SectionCard>
        <StatusBanner tone="success" title="Transparent pricing" message={quote.assignment_note} />
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
      service_type: bookingForm.serviceType,
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
      <HeaderBlock eyebrow="Final review" title="One last look before the booking is created." subtitle="This step protects against accidental taps and keeps quote confirmation explicit." visualVariant="trust" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Pickup" value={bookingForm.pickup} />
            <KeyValueRow label="Drop" value={bookingForm.drop} />
            <KeyValueRow label="Schedule" value={formatCompactTime(bookingForm.scheduleAt)} />
            <KeyValueRow label="Duration" value={bookingForm.durationLabel} />
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
      <HeaderBlock eyebrow="Booking created" title="Your ride is confirmed and moving through assignment." subtitle="We keep this stage visible so you never feel abandoned while the driver is being locked." visualVariant="status" />
      <View style={styles.stackMd}>
        <StatusBanner tone="info" title="Pending assignment" message="Backup rescue rules are in place if the first assignment starts drifting at risk." />
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
      <HeaderBlock eyebrow="Driver assigned" title="You can now see who is coming and when they should arrive." subtitle="If a reassignment happens, this card is designed to update immediately without stale driver details lingering." visualVariant="customer" />
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
        <MapPlaceholderCard title="Driver approach" subtitle="Your assigned driver, ETA, and route confidence appear here during the approach stage." />
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
      <HeaderBlock eyebrow="Start trip" title="Trip billing stays locked until you confirm the handover." subtitle="This is one of the main trust controls that makes Rydvrse feel safer and clearer than traditional driver-on-demand flows." visualVariant="trust" />
      <View style={styles.stackMd}>
        <StatusBanner tone="success" title="Driver has arrived" message="Review the pickup, add any quick handover notes, and then start the trip when you are ready." />
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
      <HeaderBlock eyebrow="Active trip" title="Live visibility should feel calm, not noisy." subtitle="The customer screen prioritizes route progress, support, trip share, and SOS without cluttering the ride experience." visualVariant="customer" />
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
        <StatusBanner tone="info" title="Need support?" message="Support and SOS stay visible during the trip and attach ride context automatically." />
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
      <HeaderBlock eyebrow="Trip complete" title="Close the ride with a clean invoice and a payment retry path if anything fails." subtitle="The payment screen preserves full fare context so a failed transaction never leaves the customer confused." visualVariant="status" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Final fare" value={formatCurrency(booking?.fare_amount_paise ?? 54900)} />
            <KeyValueRow label="Payment method" value="UPI" />
            <KeyValueRow label="Invoice status" value="Ready" />
          </View>
        </SectionCard>
        <StatusBanner tone="warning" title="Payment-safe flow" message="If UPI fails, the invoice stays visible and the user can retry without losing context." />
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
      <HeaderBlock eyebrow="After the ride" title="Capture the quality signal while the context is still fresh." subtitle="Low ratings can flow into issue capture so support can step in with the ride context already attached." visualVariant="trust" />
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
      <HeaderBlock eyebrow="Bookings" title="Upcoming and past bookings live in one timeline-friendly view." subtitle="This screen is the customer’s single source of truth outside the active trip flow." visualVariant="customer" />
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
              title={booking.service_type.replaceAll("_", " ")}
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
          <EmptyState title="Nothing booked yet" message="Your past and upcoming rides will appear here with live status, fare context, and support entry points." visualVariant="booking" />
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
        <EmptyState title="Booking detail unavailable" message="Open a booking from the list or create a new one to see the timeline and contextual actions." visualVariant="booking" />
      </Screen>
    );
  }

  return (
    <Screen>
      <HeaderBlock eyebrow="Booking detail" title="A booking timeline should reflect the latest truth, not stale state." subtitle="Actions change by lifecycle stage so customers only see the next valid thing they can do." visualVariant="trust" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Booking ID" value={booking.booking_id} />
            <KeyValueRow label="Status" value={booking.status} />
            <KeyValueRow label="Service" value={booking.service_type.replaceAll("_", " ")} />
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
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const activeBookingId = useAppSelector((state) => state.customer.activeBookingId);
  const [category, setCategory] = useState(supportCategories[0]);
  const [description, setDescription] = useState("The driver looks delayed and I want help understanding the updated ETA.");
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
    setSuccess("Support ticket created with booking context attached.");
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Help and support" title="Support should be available before, during, and after the ride." subtitle="High-stress states use calm language and keep the user’s typed context intact if something goes wrong." visualVariant="trust" />
      <View style={styles.stackMd}>
        <TextField label="Category" value={category} onChangeText={setCategory} icon="help" />
        <TextField label="Describe the issue" value={description} onChangeText={setDescription} icon="document" multiline />
        {success ? <StatusBanner tone="success" title="Ticket submitted" message={success} /> : null}
        <BottomActionBar primaryLabel="Submit ticket" onPrimaryPress={handleSubmit} primaryIcon="check" />
      </View>
    </Screen>
  );
}

export function CustomerProfileScreen() {
  const dispatch = useAppDispatch();
  const userName = useAppSelector((state) => state.session.userName);
  const mobile = useAppSelector((state) => state.session.mobileNumber);

  return (
    <Screen>
      <HeaderBlock eyebrow="Profile" title="Low-complexity account controls keep the customer focused on booking, not settings overhead." subtitle="Saved locations, city context, and logout are present here without turning the app into a settings maze." visualVariant="customer" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Name" value={userName ?? "Meera Singh"} />
            <KeyValueRow label="Mobile" value={mobile ?? "+919999999999"} />
            <KeyValueRow label="Default city" value="Bengaluru" />
            <KeyValueRow label="Saved locations" value="2" />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel="Log out" onPrimaryPress={() => dispatch(logout())} primaryIcon="logout" />
      </View>
    </Screen>
  );
}

const styles = StyleSheet.create({
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
    backgroundColor: colors.primary.soft,
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
    color: colors.primary.base
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
    width: 36,
    height: 36,
    borderRadius: 14,
    backgroundColor: colors.background.muted,
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
    backgroundColor: colors.background.surface,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.border.soft,
    padding: spacing.md,
    gap: spacing.xs
  },
  metricIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 14,
    backgroundColor: colors.primary.soft,
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
