import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, ScrollView, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { ChoiceCard } from "@/components/common/ChoiceCard";
import { EmptyState } from "@/components/common/EmptyState";
import { KeyValueRow } from "@/components/common/KeyValueRow";
import { StatusBanner } from "@/components/common/StatusBanner";
import { StatusChip } from "@/components/common/StatusChip";
import { SectionCard } from "@/components/cards/SectionCard";
import { RydvrseMapPreview } from "@/components/maps/RydvrseMapPreview";
import { TextField } from "@/components/forms/TextField";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { HeaderBlock } from "@/components/layout/HeaderBlock";
import { ScreenHeader } from "@/components/layout/ScreenHeader";
import { Screen } from "@/components/layout/Screen";
import { Skeleton } from "@/components/loaders/Skeleton";
import {
  DRIVER_SUPPORT_FAQS,
  DriverSupportFaq,
  searchDriverFaqs,
} from "@/constants/driverSupportFaq";
import { authApi } from "@/services/api/auth";
import { driverApi } from "@/services/api/driver";
import { inferBengaluruCoords } from "@/services/maps/bengaluruGeo";
import { useCurrentLocation } from "@/hooks/useCurrentLocation";
import { useAppDispatch, useAppSelector } from "@/store";
import { setActiveAssignmentId, setAvailability, setOffers, setOnboardingStatus } from "@/store/driverSlice";
import { hydrateSession, logout } from "@/store/sessionSlice";
import { colors, radius, semantic, shadows, space, spacing } from "@/theme";
import { formatCompactTime, formatCurrency } from "@/utils/format";

function resolveAvailabilityTone(status: string): "success" | "warning" | "neutral" {
  if (status === "AVAILABLE") {
    return "success";
  }

  if (status === "UNAVAILABLE") {
    return "warning";
  }

  return "neutral";
}

function resolveOnboardingTone(status: string): "info" | "warning" | "success" {
  if (status === "APPROVED") {
    return "success";
  }

  if (status === "CORRECTION_REQUIRED") {
    return "warning";
  }

  return "info";
}

function resolvePayoutTone(status: string): "success" | "warning" | "neutral" {
  const normalized = status.toLowerCase();

  if (normalized.includes("paid") || normalized.includes("settled")) {
    return "success";
  }

  if (normalized.includes("hold") || normalized.includes("review")) {
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

function DriverSignalStrip() {
  return (
    <SectionCard>
      <View style={styles.signalStrip}>
        {[
          { icon: "wallet" as const, title: "Earning clarity", subtitle: "Preview before accept" },
          { icon: "status" as const, title: "Availability", subtitle: "One-tap control" },
          { icon: "shield" as const, title: "Compliance", subtitle: "Visible approval state" }
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

export function DriverLoginScreen() {
  const navigation = useNavigation<any>();
  const [phone, setPhone] = useState("+919888888888");
  const [loading, setLoading] = useState(false);

  const handleContinue = async () => {
    setLoading(true);
    await authApi.requestOtp("DRIVER", phone);
    navigation.navigate("DriverOtp", { phone });
    setLoading(false);
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Driver app" title="Sign in" subtitle="Manage jobs, trips, and earnings." visualVariant="driver" />
      <View style={styles.stackLg}>
        <TextField label="Mobile number" value={phone} onChangeText={setPhone} keyboardType="phone-pad" icon="phone" helperText="OTP sign-in is used for driver identity in the MVP as well." />
        <BottomActionBar primaryLabel={loading ? "Sending OTP..." : "Continue"} onPrimaryPress={handleContinue} primaryDisabled={loading || phone.length < 10} primaryIcon="arrowRight" />
      </View>
    </Screen>
  );
}

export function DriverOtpScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const dispatch = useAppDispatch();
  const [otp, setOtp] = useState("123456");
  const phone = route.params?.phone ?? "+919888888888";

  const handleVerify = async () => {
    const session = await authApi.verifyOtp("DRIVER", phone, otp);
    dispatch(hydrateSession(session.data));
    dispatch(setOnboardingStatus("NOT_STARTED"));
    navigation.navigate("DriverOnboardingChecklist");
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Verify" title="Confirm your driver number" subtitle={`We sent a code to ${phone}.`} visualVariant="driver" />
      <View style={styles.stackLg}>
        <TextField label="OTP code" value={otp} onChangeText={setOtp} keyboardType="numeric" icon="ticket" helperText="Use 123456 in mock mode to continue." />
        <BottomActionBar primaryLabel="Verify OTP" secondaryLabel="Change number" onPrimaryPress={handleVerify} onSecondaryPress={() => navigation.goBack()} primaryIcon="check" secondaryIcon="phone" />
      </View>
    </Screen>
  );
}

export function DriverOnboardingChecklistScreen() {
  const navigation = useNavigation<any>();

  return (
    <Screen>
      <HeaderBlock eyebrow="Onboarding" title="Complete your checklist" subtitle="Finish required steps for approval." visualVariant="driver" />
      <View style={styles.stackMd}>
        {[
          { title: "Personal details", icon: "profile" as const },
          { title: "Driving license", icon: "document" as const },
          { title: "Identity document", icon: "id" as const },
          { title: "Bank details", icon: "bank" as const },
          { title: "Profile photo", icon: "camera" as const }
        ].map((item, index) => (
          <ChoiceCard key={item.title} title={item.title} eyebrow={`Step ${index + 1}`} subtitle={index === 0 ? "Completed in the OTP flow." : "Required before review submission."} selected={index === 0} leading={<AppIcon name={item.icon} size={20} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />} />
        ))}
        <BottomActionBar primaryLabel="Continue" onPrimaryPress={() => navigation.navigate("DriverDocumentUpload")} primaryIcon="arrowRight" />
      </View>
    </Screen>
  );
}

export function DriverDocumentUploadScreen() {
  const navigation = useNavigation<any>();
  const dispatch = useAppDispatch();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const [license, setLicense] = useState("KA-01-2026-VALID");
  const [identity, setIdentity] = useState("Aadhaar uploaded");
  const [bank, setBank] = useState("HDFC ending 1824");
  const [photo, setPhoto] = useState("Profile photo ready");

  const handleSubmit = async () => {
    if (accessToken) {
      await driverApi.submitOnboarding(accessToken, {
        license,
        identity,
        bank,
        photo
      });
    }
    dispatch(setOnboardingStatus("UNDER_REVIEW"));
    navigation.navigate("DriverOnboardingStatus");
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Document upload" title="Upload required documents" subtitle="Ops review starts after submission." visualVariant="trust" />
      <View style={styles.stackMd}>
        <TextField label="Driving license" value={license} onChangeText={setLicense} icon="document" />
        <TextField label="Identity document" value={identity} onChangeText={setIdentity} icon="id" />
        <TextField label="Bank details" value={bank} onChangeText={setBank} icon="bank" />
        <TextField label="Profile photo" value={photo} onChangeText={setPhoto} icon="camera" />
        <BottomActionBar primaryLabel="Submit for review" secondaryLabel="Back" onPrimaryPress={handleSubmit} onSecondaryPress={() => navigation.goBack()} primaryIcon="check" secondaryIcon="arrowRight" />
      </View>
    </Screen>
  );
}

export function DriverOnboardingStatusScreen() {
  const navigation = useNavigation<any>();
  const dispatch = useAppDispatch();
  const onboardingStatus = useAppSelector((state) => state.driver.onboardingStatus);
  const tone = onboardingStatus === "UNDER_REVIEW" ? "info" : onboardingStatus === "CORRECTION_REQUIRED" ? "warning" : "success";
  const title = onboardingStatus === "UNDER_REVIEW" ? "Pending review" : onboardingStatus === "CORRECTION_REQUIRED" ? "Correction needed" : "Approved";
  const message = onboardingStatus === "UNDER_REVIEW"
    ? "Ops is checking your documents. Drivers do not receive jobs until this stage is approved."
    : onboardingStatus === "CORRECTION_REQUIRED"
      ? "A document needs correction. Reopen onboarding and resubmit the specific item."
      : "Your compliance state is healthy. You can continue to the driver dashboard.";

  return (
    <Screen>
      <HeaderBlock eyebrow="Review status" title="Onboarding status" subtitle="Track review and corrections." visualVariant="status" />
      <View style={styles.stackMd}>
        <StatusBanner tone={tone as any} title={title} message={message} />
        <BottomActionBar
          primaryLabel={onboardingStatus === "APPROVED" ? "Continue to dashboard" : "Simulate approval"}
          secondaryLabel="Contact support"
          onPrimaryPress={() => {
            dispatch(setOnboardingStatus("APPROVED"));
            navigation.navigate("DriverTabs");
          }}
          onSecondaryPress={() => navigation.navigate("DriverSupport")}
          primaryIcon="check"
          secondaryIcon="help"
        />
      </View>
    </Screen>
  );
}

export function DriverHomeScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const availability = useAppSelector((state) => state.driver.availability);
  const offers = useAppSelector((state) => state.driver.offers);
  const dispatch = useAppDispatch();
  const { coords } = useCurrentLocation({ autoRequest: true, liveUpdates: true });
  const [loading, setLoading] = useState(true);
  const [earningsToday, setEarningsToday] = useState(0);

  useEffect(() => {
    let active = true;
    const load = async () => {
      if (!accessToken) {
        return;
      }
      const dashboard = await driverApi.dashboard(accessToken);
      const fetchedOffers = await driverApi.offers(accessToken);
      if (!active) {
        return;
      }
      setEarningsToday(dashboard.data.earnings_today_paise);
      dispatch(setOffers(fetchedOffers.data));
      setLoading(false);
    };
    void load();
    return () => {
      active = false;
    };
  }, [accessToken, dispatch]);

  const nextOffer = offers[0];
  const pickupCoords = inferBengaluruCoords(nextOffer?.pickup_zone ?? "Koramangala");
  const dropCoords = inferBengaluruCoords(nextOffer ? `${nextOffer.service_type} zone` : "Indiranagar");

  return (
    <Screen>
      <ScreenHeader
        title="Driver home"
        subtitle={`You are ${availability.replaceAll("_", " ").toLowerCase()} in Bengaluru`}
        showBack={false}
      />
      <View style={styles.stackMd}>
        {loading ? (
          <SectionCard>
            <Skeleton height={20} width="45%" />
            <Skeleton height={70} radiusValue={24} />
          </SectionCard>
        ) : (
          <>
            <View style={styles.driverMapCard}>
              <RydvrseMapPreview
                pickupLabel={nextOffer?.pickup_zone ?? "Koramangala"}
                dropLabel={nextOffer ? `${nextOffer.service_type.replaceAll("_", " ")} zone` : "Awaiting next offer"}
                serviceType={nextOffer?.service_type ?? "ONE_WAY_DROP"}
                distanceLabel={nextOffer ? undefined : undefined}
                durationLabel={nextOffer ? formatCompactTime(nextOffer.scheduled_at) : undefined}
                pickupCoords={pickupCoords ?? undefined}
                dropCoords={dropCoords ?? undefined}
                currentCoords={coords}
                interactive
                minimal
              />
            </View>
            <DriverSignalStrip />
            <SectionCard>
              <View style={styles.stackSm}>
                <View style={styles.badgeRow}>
                  <StatusChip label={availability.replaceAll("_", " ")} tone={resolveAvailabilityTone(availability)} />
                  <StatusChip label="Onboarding healthy" tone={resolveOnboardingTone("APPROVED")} />
                </View>
                <KeyValueRow label="Availability" value={availability} />
                <KeyValueRow label="Earnings today" value={formatCurrency(earningsToday)} />
              </View>
            </SectionCard>
            <BottomActionBar
              primaryLabel={availability === "AVAILABLE" ? "Go unavailable" : "Go available"}
              secondaryLabel="Compliance status"
              onPrimaryPress={async () => {
                if (!accessToken) {
                  return;
                }
                const next = availability === "AVAILABLE" ? "UNAVAILABLE" : "AVAILABLE";
                await driverApi.updateAvailability(accessToken, next);
                dispatch(setAvailability(next));
              }}
              onSecondaryPress={() => navigation.navigate("DriverProfile")}
              primaryIcon={availability === "AVAILABLE" ? "status" : "check"}
              secondaryIcon="shield"
            />
            <View style={styles.stackSm}>
              <SectionTitle label="Incoming work" />
              {offers.length > 0 ? (
                offers.map((offer) => (
                  <Pressable key={offer.assignment_id} onPress={() => navigation.navigate("DriverJobs")}>
                    <SectionCard>
                      <View style={styles.offerRow}>
                        <View style={styles.offerMain}>
                          <View style={styles.offerIconWrap}>
                            <AppIcon name="jobs" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
                          </View>
                          <View style={{ flex: 1 }}>
                            <View style={styles.offerHeading}>
                              <AppText variant="section">{offer.service_type.replaceAll("_", " ")}</AppText>
                              <StatusChip label="Offer live" tone="info" />
                            </View>
                            <AppText variant="body">{offer.pickup_zone} • {formatCompactTime(offer.scheduled_at)}</AppText>
                          </View>
                        </View>
                        <View style={styles.offerAside}>
                          <AppText variant="bodyStrong">{formatCurrency(offer.estimated_earning_paise)}</AppText>
                          <AppText variant="caption" style={{ color: colors.brand.primary }}>Preview earning</AppText>
                        </View>
                      </View>
                    </SectionCard>
                  </Pressable>
                ))
              ) : (
                <EmptyState title="No jobs yet" message="Stay available to receive the next scheduled offer." visualVariant="jobs" />
              )}
            </View>
          </>
        )}
      </View>
    </Screen>
  );
}

export function DriverJobOfferScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const offers = useAppSelector((state) => state.driver.offers);
  const dispatch = useAppDispatch();
  const currentOffer = offers[0];

  const handleAccept = async () => {
    if (!accessToken || !currentOffer) {
      return;
    }
    await driverApi.acceptOffer(accessToken, currentOffer.assignment_id);
    dispatch(setActiveAssignmentId(currentOffer.assignment_id));
    navigation.navigate("DriverAssignmentDetail");
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Incoming offer" title="Review and accept offer" subtitle="Earning preview shown before accept." visualVariant="driver" />
      <View style={styles.stackMd}>
        {currentOffer ? (
          <>
            <SectionCard>
              <View style={styles.stackSm}>
                <KeyValueRow label="Service" value={currentOffer.service_type.replaceAll("_", " ")} />
                <KeyValueRow label="Pickup zone" value={currentOffer.pickup_zone} />
                <KeyValueRow label="Schedule" value={formatCompactTime(currentOffer.scheduled_at)} />
                <KeyValueRow label="Expected duration" value={`${currentOffer.expected_duration_minutes} mins`} />
                <KeyValueRow label="Estimated earning" value={formatCurrency(currentOffer.estimated_earning_paise)} />
              </View>
            </SectionCard>
            <BottomActionBar primaryLabel="Accept" secondaryLabel="Decline" onPrimaryPress={handleAccept} onSecondaryPress={() => dispatch(setOffers([]))} primaryIcon="check" secondaryIcon="alert" />
          </>
        ) : (
          <EmptyState title="No active offer" message="Offer expiry and decline handling should always bring the driver back here cleanly." actionLabel="Back to home" visualVariant="jobs" onAction={() => navigation.navigate("DriverHome")} />
        )}
      </View>
    </Screen>
  );
}

export function DriverAssignmentDetailScreen() {
  const navigation = useNavigation<any>();
  const assignmentId = useAppSelector((state) => state.driver.activeAssignmentId);
  const offers = useAppSelector((state) => state.driver.offers);
  const offer = useMemo(() => offers.find((item) => item.assignment_id === assignmentId) ?? offers[0], [assignmentId, offers]);

  return (
    <Screen>
      <HeaderBlock eyebrow="Assignment" title="Trip details" subtitle="Pickup context and next actions." visualVariant="driver" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Assignment" value={offer?.assignment_id ?? "assignment-001"} />
            <KeyValueRow label="Pickup zone" value={offer?.pickup_zone ?? "Koramangala"} />
            <KeyValueRow label="Earning preview" value={formatCurrency(offer?.estimated_earning_paise ?? 29000)} />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel="Navigate to pickup" secondaryLabel="Need support" onPrimaryPress={() => navigation.navigate("DriverPickup")} onSecondaryPress={() => navigation.navigate("DriverSupport")} primaryIcon="route" secondaryIcon="help" />
      </View>
    </Screen>
  );
}

export function DriverPickupScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const assignmentId = useAppSelector((state) => state.driver.activeAssignmentId) ?? "trip-001";
  const { coords } = useCurrentLocation({ autoRequest: true, liveUpdates: true });
  const [marking, setMarking] = useState(false);

  const pickupCoords = inferBengaluruCoords("Koramangala 4th Block");
  const dropCoords = inferBengaluruCoords("Whitefield Main Road");

  const handleArrived = async () => {
    if (!accessToken) {
      return;
    }
    setMarking(true);
    await driverApi.markArrived(accessToken, assignmentId);
    navigation.navigate("DriverAwaitingStart");
    setMarking(false);
  };

  return (
    <Screen>
      <ScreenHeader title="Pickup navigation" subtitle="Navigate to pickup and mark arrival when ready." />
      <View style={styles.stackMd}>
        <RydvrseMapPreview
          pickupLabel="Customer pickup"
          dropLabel="Drop destination"
          serviceType="ONE_WAY_DROP"
          distanceLabel="3.2 km"
          durationLabel="8 min"
          pickupCoords={pickupCoords ?? undefined}
          dropCoords={dropCoords ?? undefined}
          currentCoords={coords}
          interactive
        />
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Customer" value="Meera Singh" />
            <KeyValueRow label="Pickup" value="Koramangala 4th Block" />
            <KeyValueRow label="ETA" value="8 min (3.2 km)" />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel={marking ? "Marking..." : "Mark arrived"} secondaryLabel="Pickup issue" onPrimaryPress={handleArrived} onSecondaryPress={() => navigation.navigate("DriverSupport")} primaryDisabled={marking} primaryIcon="check" secondaryIcon="alert" />
      </View>
    </Screen>
  );
}

export function DriverAwaitingStartScreen() {
  const navigation = useNavigation<any>();
  const [otp, setOtp] = useState("");
  const [error, setError] = useState("");
  const expectedOtp = "4821";

  const handleStart = () => {
    const cleaned = otp.replace(/\s/g, "");
    if (cleaned.length < 4) {
      setError("Enter the 4-digit OTP the customer reads out.");
      return;
    }
    if (cleaned !== expectedOtp) {
      setError("OTP does not match. Double-check with the customer.");
      return;
    }
    setError("");
    navigation.navigate("DriverActiveTrip");
  };

  return (
    <Screen>
      <ScreenHeader title="Verify pickup OTP" subtitle="Ask the customer for the 4-digit code before starting." />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <AppText variant="bodyStrong">Ask the customer</AppText>
            <AppText variant="caption">Say: "Could you share the 4-digit pickup OTP from your app?" Enter it here to start the trip.</AppText>
            <TextField
              label="Pickup OTP"
              placeholder="4-digit code"
              value={otp}
              onChangeText={setOtp}
              icon="shield"
              keyboardType="numeric"
            />
            <AppText variant="caption">Test mode OTP: {expectedOtp}</AppText>
          </View>
        </SectionCard>
        {error ? <StatusBanner tone="warning" title="Cannot start yet" message={error} /> : null}
        <BottomActionBar
          primaryLabel="Start trip"
          secondaryLabel="Pickup issue"
          onPrimaryPress={handleStart}
          onSecondaryPress={() => navigation.navigate("DriverSupport")}
          primaryDisabled={otp.trim().length < 4}
          primaryIcon="check"
          secondaryIcon="alert"
        />
      </View>
    </Screen>
  );
}

export function DriverActiveTripScreen() {
  const navigation = useNavigation<any>();
  const { coords } = useCurrentLocation({ autoRequest: true, liveUpdates: true });
  const pickupCoords = inferBengaluruCoords("Koramangala 4th Block");
  const dropCoords = inferBengaluruCoords("Whitefield Main Road");

  return (
    <Screen>
      <HeaderBlock eyebrow="Active trip" title="Trip in progress" subtitle="Navigation, support, and completion." visualVariant="driver" />
      <View style={styles.stackMd}>
        <RydvrseMapPreview
          pickupLabel="Koramangala 4th Block"
          dropLabel="Whitefield Main Road"
          serviceType="ONE_WAY_DROP"
          pickupCoords={pickupCoords ?? undefined}
          dropCoords={dropCoords ?? undefined}
          currentCoords={coords}
          distanceLabel="31 km"
          durationLabel="105 min"
          interactive
        />
        <StatusBanner tone="warning" title="Weak connection handling" message="If connectivity drops, the UI should stay clear that the trip is still active and retry safely." />
        <BottomActionBar primaryLabel="Complete trip" secondaryLabel="Trip issue" onPrimaryPress={() => navigation.navigate("DriverTripComplete")} onSecondaryPress={() => navigation.navigate("DriverSupport")} primaryIcon="check" secondaryIcon="alert" />
      </View>
    </Screen>
  );
}

export function DriverTripCompleteScreen() {
  const navigation = useNavigation<any>();
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const assignmentId = useAppSelector((state) => state.driver.activeAssignmentId) ?? "trip-001";
  const [done, setDone] = useState(false);

  useEffect(() => {
    let active = true;
    const complete = async () => {
      if (!accessToken) {
        return;
      }
      await driverApi.completeTrip(accessToken, assignmentId);
      if (active) {
        setDone(true);
      }
    };
    void complete();
    return () => {
      active = false;
    };
  }, [accessToken, assignmentId]);

  return (
    <Screen>
      <HeaderBlock eyebrow="Trip complete" title="Payout summary" subtitle="Visible earning breakdown." visualVariant="status" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Arrival fee" value={formatCurrency(5000)} />
            <KeyValueRow label="Service payout" value={formatCurrency(20000)} />
            <KeyValueRow label="Night bonus" value={formatCurrency(4000)} />
            <KeyValueRow label="Total" value={formatCurrency(29000)} />
          </View>
        </SectionCard>
        {done ? <StatusBanner tone="success" title="Trip closed" message="The payout summary is now traceable to this trip in the earnings ledger." /> : null}
        <BottomActionBar primaryLabel="Done" secondaryLabel="Raise issue" onPrimaryPress={() => navigation.navigate("DriverTabs")} onSecondaryPress={() => navigation.navigate("DriverSupport")} primaryIcon="arrowRight" secondaryIcon="help" />
      </View>
    </Screen>
  );
}

export function DriverEarningsScreen() {
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const [loading, setLoading] = useState(true);
  const [ledger, setLedger] = useState<Array<{ trip_id: string; title: string; amount_paise: number; payout_status: string }>>([]);

  useEffect(() => {
    let active = true;
    const load = async () => {
      if (!accessToken) {
        return;
      }
      const response = await driverApi.earningsLedger(accessToken);
      if (!active) {
        return;
      }
      setLedger(response.data);
      setLoading(false);
    };
    void load();
    return () => {
      active = false;
    };
  }, [accessToken]);

  return (
    <Screen>
      <HeaderBlock eyebrow="Earnings" title="Payout history" subtitle="Trip-wise payout ledger." visualVariant="driver" />
      <View style={styles.stackMd}>
        {loading ? (
          <>
            <Skeleton height={88} radiusValue={24} />
            <Skeleton height={88} radiusValue={24} />
          </>
        ) : (
          ledger.map((item) => (
            <SectionCard key={item.trip_id}>
              <View style={styles.offerRow}>
                <View style={styles.offerMain}>
                  <View style={styles.offerIconWrap}>
                    <AppIcon name="earnings" size={18} color={colors.brand.primary} secondaryColor={colors.neutral[400]} />
                  </View>
                  <View style={{ flex: 1 }}>
                    <View style={styles.offerHeading}>
                      <AppText variant="section">{item.title}</AppText>
                      <StatusChip label={item.payout_status} tone={resolvePayoutTone(item.payout_status)} />
                    </View>
                    <AppText variant="body">Trip payout ledger entry</AppText>
                  </View>
                </View>
                <View style={styles.offerAside}>
                  <AppText variant="bodyStrong">{formatCurrency(item.amount_paise)}</AppText>
                  <AppText variant="caption" style={{ color: colors.brand.primary }}>Net payout</AppText>
                </View>
              </View>
            </SectionCard>
          ))
        )}
      </View>
    </Screen>
  );
}

function DriverFaqCard({
  faq,
  onPress,
}: {
  faq: DriverSupportFaq;
  onPress: () => void;
}) {
  return (
    <Pressable
      style={styles.faqCard}
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={`Open answer for ${faq.question}`}
    >
      <View style={styles.faqIcon}>
        <AppIcon name="help" size={17} color={colors.brand.primary} secondaryColor={colors.brand.strong} />
      </View>
      <View style={styles.faqCopy}>
        <AppText variant="caption" style={styles.faqCategory}>{faq.category}</AppText>
        <AppText variant="bodyStrong" numberOfLines={2}>{faq.question}</AppText>
      </View>
      <AppIcon name="arrowRight" size={16} color={colors.neutral[400]} secondaryColor={colors.neutral[400]} />
    </Pressable>
  );
}

export function DriverSupportScreen() {
  const navigation = useNavigation<any>();
  const [query, setQuery] = useState("");

  const trimmedQuery = query.trim();
  const filtered = useMemo(() => searchDriverFaqs(trimmedQuery), [trimmedQuery]);
  const visibleFaqs =
    trimmedQuery.length > 0
      ? filtered
      : DRIVER_SUPPORT_FAQS.slice(0, 3);

  const openChat = useCallback(
    (faq?: DriverSupportFaq) => {
      navigation.navigate("DriverSupportChat", {
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
        onBack={() => navigation.navigate("DriverHome")}
      />
      <ScrollView contentContainerStyle={styles.stackMd} showsVerticalScrollIndicator={false}>
        <TextField
          label="Search help"
          placeholder="Try 'payout', 'pickup', or 'document'"
          value={query}
          onChangeText={setQuery}
          icon="help"
        />

        <View style={styles.supportQuickRow}>
          <Pressable style={styles.supportQuickCard} onPress={() => openChat()} accessibilityRole="button" accessibilityLabel="Chat with a driver specialist">
            <View style={styles.supportQuickIcon}>
              <AppIcon name="help" size={18} color={semantic.text.onBrand} secondaryColor={semantic.text.onBrand} />
            </View>
            <AppText variant="bodyStrong">Chat with us</AppText>
            <AppText variant="caption" style={styles.supportQuickHint}>Avg wait &lt; 2 min</AppText>
          </Pressable>
          <Pressable
            style={styles.supportQuickCardAlt}
            onPress={() => navigation.navigate("DriverEarnings")}
            accessibilityRole="button"
            accessibilityLabel="Open earnings"
          >
            <View style={styles.supportQuickIconAlt}>
              <AppIcon name="wallet" size={18} color={semantic.text.primary} secondaryColor={colors.brand.strong} />
            </View>
            <AppText variant="bodyStrong">Earnings</AppText>
            <AppText variant="caption" style={styles.supportQuickHint}>Payout issues</AppText>
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
              <DriverFaqCard key={faq.id} faq={faq} onPress={() => openChat(faq)} />
            ))}
          </View>
        )}
      </ScrollView>
    </Screen>
  );
}

export function DriverProfileScreen() {
  const dispatch = useAppDispatch();
  const navigation = useNavigation<any>();
  const availability = useAppSelector((state) => state.driver.availability);
  const onboardingStatus = useAppSelector((state) => state.driver.onboardingStatus);
  const userName = useAppSelector((state) => state.session.userName);
  const mobile = useAppSelector((state) => state.session.mobileNumber);
  const offers = useAppSelector((state) => state.driver.offers);

  const fullName = userName ?? "Ravi Kumar";
  const initials = fullName
    .split(" ")
    .map((part) => part.charAt(0))
    .slice(0, 2)
    .join("")
    .toUpperCase();

  const tripCount = offers.length;
  const acceptedCount = Math.max(0, offers.length - 1);

  return (
    <Screen scrollable={false}>
      <ScreenHeader
        title="Profile"
        subtitle="Account and status"
        onBack={() => navigation.navigate("DriverHome")}
      />
      <ScrollView contentContainerStyle={styles.profileScroll} showsVerticalScrollIndicator={false}>
        <View style={styles.profileHero}>
          <View style={styles.profileAvatarOuter}>
            <View style={styles.profileAvatarInner}>
              <AppText variant="section" style={styles.profileAvatarText}>{initials || "R"}</AppText>
            </View>
          </View>
          <AppText variant="section" style={styles.profileName}>{fullName}</AppText>
          <AppText variant="caption" style={styles.profileHandle}>{mobile ?? "+91 99999 99999"}</AppText>

          <View style={styles.profileStatsRow}>
            <View style={styles.profileStatCell}>
              <AppText variant="section">{tripCount}</AppText>
              <AppText variant="caption">Offers</AppText>
            </View>
            <View style={styles.profileStatDivider} />
            <View style={styles.profileStatCell}>
              <AppText variant="section">{acceptedCount}</AppText>
              <AppText variant="caption">Accepted</AppText>
            </View>
            <View style={styles.profileStatDivider} />
            <View style={styles.profileStatCell}>
              <AppText variant="section">4.9</AppText>
              <AppText variant="caption">Rating</AppText>
            </View>
          </View>

          <View style={styles.profileBadgeRow}>
            <StatusChip label={availability.replaceAll("_", " ")} tone={resolveAvailabilityTone(availability)} />
            <StatusChip label={onboardingStatus.replaceAll("_", " ")} tone={resolveOnboardingTone(onboardingStatus)} />
          </View>
        </View>

        <SectionCard>
          <View style={styles.stackSm}>
            <KeyValueRow label="Driver name" value={fullName} />
            <KeyValueRow label="Mobile" value={mobile ?? "+91 99999 99999"} />
            <KeyValueRow label="Availability" value={availability} />
            <KeyValueRow label="Onboarding" value={onboardingStatus} />
          </View>
        </SectionCard>

        <View style={styles.profileActionList}>
          <Pressable
            style={styles.profileActionRow}
            onPress={() => navigation.navigate("DriverEarnings")}
            accessibilityRole="button"
            accessibilityLabel="View earnings"
          >
            <View style={styles.profileActionIcon}>
              <AppIcon name="wallet" size={17} color={colors.brand.primary} secondaryColor={colors.brand.strong} />
            </View>
            <View style={styles.profileActionCopy}>
              <AppText variant="bodyStrong">Earnings</AppText>
            </View>
          </Pressable>
          <Pressable
            style={styles.profileActionRow}
            onPress={() => navigation.navigate("DriverSupport")}
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
            onPress={() => {
              dispatch(setOnboardingStatus("NOT_STARTED"));
              dispatch(logout());
            }}
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

const styles = StyleSheet.create({
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
  badgeRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: spacing.xs
  },
  offerRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: spacing.md,
    flexWrap: "wrap"
  },
  offerMain: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
    flex: 1,
    minWidth: 240
  },
  offerIconWrap: {
    width: 48,
    height: 48,
    borderRadius: 18,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center"
  },
  offerHeading: {
    gap: spacing.xs
  },
  offerAside: {
    minWidth: 108,
    alignItems: "flex-end",
    gap: spacing.xs
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
  driverMapCard: {
    height: 200,
    borderRadius: radius.xl,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  supportQuickRow: {
    flexDirection: "row",
    gap: spacing.sm,
  },
  supportQuickCard: {
    flex: 1,
    borderRadius: radius.lg,
    padding: spacing.md,
    backgroundColor: colors.brand.primary,
    gap: spacing.xs,
    ...shadows.sm,
  },
  supportQuickCardAlt: {
    flex: 1,
    borderRadius: radius.lg,
    padding: spacing.md,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    gap: spacing.xs,
  },
  supportQuickIcon: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: colors.brand.strong,
    alignItems: "center",
    justifyContent: "center",
  },
  supportQuickIconAlt: {
    width: 36,
    height: 36,
    borderRadius: 12,
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
    alignItems: "baseline",
  },
  sectionHeaderMeta: {
    color: semantic.text.secondary,
  },
  faqCard: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  faqIcon: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  faqCopy: {
    flex: 1,
    gap: 2,
  },
  faqCategory: {
    color: semantic.text.secondary,
    textTransform: "uppercase",
    letterSpacing: 1,
  },
  viewMoreButton: {
    alignSelf: "center",
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.sm,
    borderRadius: 20,
    backgroundColor: semantic.bg.muted,
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
    borderRadius: 12,
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
    borderRadius: 16,
    backgroundColor: colors.brand.primary,
  },
  supportContactCtaText: {
    color: semantic.text.onBrand,
  },
  profileScroll: {
    gap: spacing.md,
    paddingBottom: space[8],
  },
  profileHero: {
    alignItems: "center",
    gap: spacing.xs,
    paddingVertical: spacing.md,
  },
  profileAvatarOuter: {
    width: 104,
    height: 104,
    borderRadius: 52,
    backgroundColor: colors.brand.primary,
    padding: 4,
    alignItems: "center",
    justifyContent: "center",
  },
  profileAvatarInner: {
    width: 96,
    height: 96,
    borderRadius: 48,
    backgroundColor: semantic.bg.surface,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 3,
    borderColor: semantic.bg.surface,
  },
  profileAvatarText: {
    fontSize: 30,
    color: semantic.text.primary,
  },
  profileName: {
    marginTop: spacing.xs,
  },
  profileHandle: {
    color: semantic.text.secondary,
  },
  profileStatsRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.md,
    marginTop: spacing.sm,
  },
  profileStatCell: {
    alignItems: "center",
    minWidth: 72,
  },
  profileStatDivider: {
    width: 1,
    height: 28,
    backgroundColor: semantic.border.soft,
  },
  profileBadgeRow: {
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "center",
    gap: spacing.xs,
    marginTop: spacing.sm,
  },
  profileActionList: {
    gap: spacing.xs,
  },
  profileActionRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: spacing.sm,
    padding: spacing.md,
    borderRadius: radius.lg,
    backgroundColor: semantic.bg.surface,
    borderWidth: 1,
    borderColor: semantic.border.soft,
  },
  profileActionIcon: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: colors.brand.soft,
    alignItems: "center",
    justifyContent: "center",
  },
  profileActionIconDanger: {
    backgroundColor: "rgba(255, 95, 95, 0.15)",
  },
  profileActionCopy: {
    flex: 1,
    gap: 2,
  },
});
