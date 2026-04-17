import React, { useEffect, useMemo, useState } from "react";
import { Pressable, StyleSheet, View } from "react-native";
import { useNavigation, useRoute } from "@react-navigation/native";

import { AppIcon } from "@/assets/icons/AppIcon";
import { AppText } from "@/components/common/AppText";
import { ChoiceCard } from "@/components/common/ChoiceCard";
import { EmptyState } from "@/components/common/EmptyState";
import { KeyValueRow } from "@/components/common/KeyValueRow";
import { StatusBanner } from "@/components/common/StatusBanner";
import { StatusChip } from "@/components/common/StatusChip";
import { SectionCard } from "@/components/cards/SectionCard";
import { MapPlaceholderCard } from "@/components/cards/MapPlaceholderCard";
import { TextField } from "@/components/forms/TextField";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { HeaderBlock } from "@/components/layout/HeaderBlock";
import { Screen } from "@/components/layout/Screen";
import { Skeleton } from "@/components/loaders/Skeleton";
import { driverSupportCategories } from "@/constants/support";
import { authApi } from "@/services/api/auth";
import { driverApi } from "@/services/api/driver";
import { useAppDispatch, useAppSelector } from "@/store";
import { setActiveAssignmentId, setAvailability, setOffers, setOnboardingStatus } from "@/store/driverSlice";
import { hydrateSession, logout } from "@/store/sessionSlice";
import { colors, semantic, spacing } from "@/theme";
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
      <HeaderBlock eyebrow="Driver app" title="Sign in to manage jobs, availability, onboarding, and earnings." subtitle="The driver UI is task-first and surfaces earning clarity before acceptance." visualVariant="driver" />
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
      <HeaderBlock eyebrow="Onboarding" title="Make progress explicit so drivers know exactly what blocks approval." subtitle="The checklist is ordered to reduce abandonment and give operations clean review inputs." visualVariant="driver" />
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
      <HeaderBlock eyebrow="Document upload" title="Document quality matters because approval quality affects marketplace trust." subtitle="The MVP keeps uploads simple and lets ops review corrections manually." visualVariant="trust" />
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
      <HeaderBlock eyebrow="Review status" title="Keep approval status visible so nothing fails as a surprise." subtitle="Correction reasons and compliance blockers should be shown before they prevent work." visualVariant="status" />
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

  return (
    <Screen>
      <HeaderBlock eyebrow="Driver home" title="A task-first dashboard keeps focus on jobs, availability, and payout clarity." subtitle="If compliance breaks later, availability should block here before the driver wastes time waiting for work." visualVariant="driver" />
      <View style={styles.stackMd}>
        {loading ? (
          <SectionCard>
            <Skeleton height={20} width="45%" />
            <Skeleton height={70} radiusValue={24} />
          </SectionCard>
        ) : (
          <>
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
      <HeaderBlock eyebrow="Incoming offer" title="Drivers should always see the earning preview before they commit." subtitle="This is one of the key trust and retention levers on the supply side." visualVariant="driver" />
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
      <HeaderBlock eyebrow="Accepted assignment" title="Once accepted, the driver needs the full trip context without distraction." subtitle="If reassignment or cancellation happens later, this view should update immediately and remove stale actions." visualVariant="driver" />
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
  const [marking, setMarking] = useState(false);

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
      <HeaderBlock eyebrow="Pickup approach" title="Arrival is logged, but the trip still waits for the customer's start confirmation." subtitle="This keeps billing fair and aligns the driver flow with the trust-first product rule." visualVariant="trust" />
      <View style={styles.stackMd}>
        <MapPlaceholderCard title="Pickup navigation" subtitle="Navigation context, ETA, and issue raising sit together here." />
        <BottomActionBar primaryLabel={marking ? "Marking..." : "Mark arrived"} secondaryLabel="Pickup issue" onPrimaryPress={handleArrived} onSecondaryPress={() => navigation.navigate("DriverSupport")} primaryDisabled={marking} primaryIcon="check" secondaryIcon="alert" />
      </View>
    </Screen>
  );
}

export function DriverAwaitingStartScreen() {
  const navigation = useNavigation<any>();

  return (
    <Screen>
      <HeaderBlock eyebrow="Waiting state" title="The driver sees a simple hold state until the customer explicitly starts the trip." subtitle="This prevents accidental early activation and keeps pickup disputes auditable." visualVariant="status" />
      <View style={styles.stackMd}>
        <StatusBanner tone="info" title="Waiting for customer confirmation" message="If the customer is not reachable or the pickup is wrong, raise the issue instead of forcing trip start." />
        <BottomActionBar primaryLabel="Simulate customer start" secondaryLabel="Pickup issue" onPrimaryPress={() => navigation.navigate("DriverActiveTrip")} onSecondaryPress={() => navigation.navigate("DriverSupport")} primaryIcon="check" secondaryIcon="alert" />
      </View>
    </Screen>
  );
}

export function DriverActiveTripScreen() {
  const navigation = useNavigation<any>();

  return (
    <Screen>
      <HeaderBlock eyebrow="Active trip" title="Trip execution stays intentionally uncluttered for the driver." subtitle="Destination, trip state, navigation, support, and completion are the only controls that matter here." visualVariant="driver" />
      <View style={styles.stackMd}>
        <MapPlaceholderCard title="Trip route" subtitle="Route guidance and trip timer appear here while the driver stays focused on execution." />
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
      <HeaderBlock eyebrow="Trip complete" title="Driver earnings should be visible immediately and without hidden deductions." subtitle="This screen is where payout trust gets reinforced or damaged, so the breakdown must stay clean." visualVariant="status" />
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
      <HeaderBlock eyebrow="Earnings" title="Historical earnings keep payout logic transparent over time." subtitle="Trip-level entries make it easier for drivers to trust the platform and challenge real issues fairly." visualVariant="driver" />
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

export function DriverSupportScreen() {
  const accessToken = useAppSelector((state) => state.session.accessToken);
  const [category, setCategory] = useState(driverSupportCategories[0]);
  const [description, setDescription] = useState("Customer is not reachable at the pickup and I need ops guidance.");
  const [success, setSuccess] = useState("");

  const handleSubmit = async () => {
    if (!accessToken) {
      return;
    }
    await driverApi.createSupportTicket(accessToken, {
      category,
      description
    });
    setSuccess("Driver support ticket created successfully.");
  };

  return (
    <Screen>
      <HeaderBlock eyebrow="Driver support" title="Drivers need a fast support path for pickup, trip, payout, and compliance issues." subtitle="Context is auto-attached in the final implementation; the UI should keep the reporting form calm and short." visualVariant="trust" />
      <View style={styles.stackMd}>
        <TextField label="Category" value={category} onChangeText={setCategory} icon="help" />
        <TextField label="Describe the issue" value={description} onChangeText={setDescription} icon="document" multiline />
        {success ? <StatusBanner tone="success" title="Ticket submitted" message={success} /> : null}
        <BottomActionBar primaryLabel="Submit issue" onPrimaryPress={handleSubmit} primaryIcon="check" />
      </View>
    </Screen>
  );
}

export function DriverProfileScreen() {
  const dispatch = useAppDispatch();
  const availability = useAppSelector((state) => state.driver.availability);
  const onboardingStatus = useAppSelector((state) => state.driver.onboardingStatus);

  return (
    <Screen>
      <HeaderBlock eyebrow="Driver profile" title="Compliance health should be visible before it blocks earning opportunities." subtitle="This screen keeps profile basics, document state, and logout in one simple maintenance view." visualVariant="driver" />
      <View style={styles.stackMd}>
        <SectionCard>
          <View style={styles.stackSm}>
            <View style={styles.badgeRow}>
              <StatusChip label={availability.replaceAll("_", " ")} tone={resolveAvailabilityTone(availability)} />
              <StatusChip label={onboardingStatus.replaceAll("_", " ")} tone={resolveOnboardingTone(onboardingStatus)} />
            </View>
            <KeyValueRow label="Driver name" value="Ravi Kumar" />
            <KeyValueRow label="Availability" value={availability} />
            <KeyValueRow label="Onboarding" value={onboardingStatus} />
            <KeyValueRow label="Compliance" value="Healthy" />
          </View>
        </SectionCard>
        <BottomActionBar primaryLabel="Log out" primaryIcon="logout" onPrimaryPress={() => {
          dispatch(setOnboardingStatus("NOT_STARTED"));
          dispatch(logout());
        }} />
      </View>
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
  }
});
