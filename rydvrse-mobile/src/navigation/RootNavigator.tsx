import React from "react";
import { NavigationContainer, DefaultTheme } from "@react-navigation/native";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { createNativeStackNavigator } from "@react-navigation/native-stack";

import { AppIcon } from "@/assets/icons/AppIcon";
import { env } from "@/constants/env";
import { RolePickerScreen } from "@/screens/shared/RolePickerScreen";
import {
  CustomerActiveTripScreen,
  CustomerAssignedDriverScreen,
  CustomerBookingDetailScreen,
  CustomerBookingReviewScreen,
  CustomerBookingsScreen,
  CustomerBookingStatusScreen,
  CustomerHomeScreen,
  CustomerLoginScreen,
  CustomerOtpScreen,
  CustomerPaymentScreen,
  CustomerProfileScreen,
  CustomerProfileSetupScreen,
  CustomerQuoteScreen,
  CustomerRatingIssueScreen,
  CustomerServiceSetupScreen,
  CustomerStartTripScreen,
  CustomerSupportScreen
} from "@/screens/customer/CustomerScreens";
import {
  DriverActiveTripScreen,
  DriverAssignmentDetailScreen,
  DriverAwaitingStartScreen,
  DriverDocumentUploadScreen,
  DriverEarningsScreen,
  DriverHomeScreen,
  DriverJobOfferScreen,
  DriverLoginScreen,
  DriverOnboardingChecklistScreen,
  DriverOnboardingStatusScreen,
  DriverOtpScreen,
  DriverPickupScreen,
  DriverProfileScreen,
  DriverSupportScreen,
  DriverTripCompleteScreen
} from "@/screens/driver/DriverScreens";
import { useAppSelector } from "@/store";
import { colors } from "@/theme";

const RootStack = createNativeStackNavigator();
const CustomerStack = createNativeStackNavigator();
const DriverStack = createNativeStackNavigator();
const CustomerTabs = createBottomTabNavigator();
const DriverTabs = createBottomTabNavigator();

function CustomerTabNavigator() {
  return (
    <CustomerTabs.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary.base,
        tabBarInactiveTintColor: colors.text.muted,
        tabBarStyle: {
          backgroundColor: colors.background.surface,
          borderTopColor: colors.border.soft,
          height: 72,
          paddingTop: 8,
          paddingBottom: 10
        },
        tabBarLabelStyle: {
          fontSize: 11
        }
      }}
    >
      <CustomerTabs.Screen name="CustomerHome" component={CustomerHomeScreen} options={{ title: "Home", tabBarIcon: ({ color }) => <AppIcon name="home" color={color} secondaryColor={color} /> }} />
      <CustomerTabs.Screen name="CustomerBookings" component={CustomerBookingsScreen} options={{ title: "Bookings", tabBarIcon: ({ color }) => <AppIcon name="calendar" color={color} secondaryColor={color} /> }} />
      <CustomerTabs.Screen name="CustomerSupport" component={CustomerSupportScreen} options={{ title: "Help", tabBarIcon: ({ color }) => <AppIcon name="help" color={color} secondaryColor={color} /> }} />
      <CustomerTabs.Screen name="CustomerProfile" component={CustomerProfileScreen} options={{ title: "Profile", tabBarIcon: ({ color }) => <AppIcon name="profile" color={color} secondaryColor={color} /> }} />
    </CustomerTabs.Navigator>
  );
}

function DriverTabNavigator() {
  return (
    <DriverTabs.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary.base,
        tabBarInactiveTintColor: colors.text.muted,
        tabBarStyle: {
          backgroundColor: colors.background.surface,
          borderTopColor: colors.border.soft,
          height: 72,
          paddingTop: 8,
          paddingBottom: 10
        },
        tabBarLabelStyle: {
          fontSize: 11
        }
      }}
    >
      <DriverTabs.Screen name="DriverHome" component={DriverHomeScreen} options={{ title: "Home", tabBarIcon: ({ color }) => <AppIcon name="home" color={color} secondaryColor={color} /> }} />
      <DriverTabs.Screen name="DriverJobs" component={DriverJobOfferScreen} options={{ title: "Jobs", tabBarIcon: ({ color }) => <AppIcon name="jobs" color={color} secondaryColor={color} /> }} />
      <DriverTabs.Screen name="DriverEarnings" component={DriverEarningsScreen} options={{ title: "Earnings", tabBarIcon: ({ color }) => <AppIcon name="wallet" color={color} secondaryColor={color} /> }} />
      <DriverTabs.Screen name="DriverSupport" component={DriverSupportScreen} options={{ title: "Support", tabBarIcon: ({ color }) => <AppIcon name="help" color={color} secondaryColor={color} /> }} />
      <DriverTabs.Screen name="DriverProfile" component={DriverProfileScreen} options={{ title: "Profile", tabBarIcon: ({ color }) => <AppIcon name="profile" color={color} secondaryColor={color} /> }} />
    </DriverTabs.Navigator>
  );
}

function CustomerNavigator() {
  const authenticated = useAppSelector((state) => state.session.status === "authenticated" && state.session.activeRole === "CUSTOMER");
  const profileComplete = useAppSelector((state) => state.customer.profileComplete);

  return (
    <CustomerStack.Navigator screenOptions={{ headerShown: false }}>
      {!authenticated ? (
        <>
          <CustomerStack.Screen name="CustomerLogin" component={CustomerLoginScreen} />
          <CustomerStack.Screen name="CustomerOtp" component={CustomerOtpScreen} />
          <CustomerStack.Screen name="CustomerProfileSetup" component={CustomerProfileSetupScreen} />
        </>
      ) : !profileComplete ? (
        <CustomerStack.Screen name="CustomerProfileSetup" component={CustomerProfileSetupScreen} />
      ) : (
        <>
          <CustomerStack.Screen name="CustomerTabs" component={CustomerTabNavigator} />
          <CustomerStack.Screen name="CustomerServiceSetup" component={CustomerServiceSetupScreen} />
          <CustomerStack.Screen name="CustomerQuote" component={CustomerQuoteScreen} />
          <CustomerStack.Screen name="CustomerBookingReview" component={CustomerBookingReviewScreen} />
          <CustomerStack.Screen name="CustomerBookingStatus" component={CustomerBookingStatusScreen} />
          <CustomerStack.Screen name="CustomerAssignedDriver" component={CustomerAssignedDriverScreen} />
          <CustomerStack.Screen name="CustomerStartTrip" component={CustomerStartTripScreen} />
          <CustomerStack.Screen name="CustomerActiveTrip" component={CustomerActiveTripScreen} />
          <CustomerStack.Screen name="CustomerPayment" component={CustomerPaymentScreen} />
          <CustomerStack.Screen name="CustomerRatingIssue" component={CustomerRatingIssueScreen} />
          <CustomerStack.Screen name="CustomerBookingDetail" component={CustomerBookingDetailScreen} />
        </>
      )}
    </CustomerStack.Navigator>
  );
}

function DriverNavigator() {
  const authenticated = useAppSelector((state) => state.session.status === "authenticated" && state.session.activeRole === "DRIVER");
  const onboardingStatus = useAppSelector((state) => state.driver.onboardingStatus);

  return (
    <DriverStack.Navigator screenOptions={{ headerShown: false }}>
      {!authenticated ? (
        <>
          <DriverStack.Screen name="DriverLogin" component={DriverLoginScreen} />
          <DriverStack.Screen name="DriverOtp" component={DriverOtpScreen} />
          <DriverStack.Screen name="DriverOnboardingChecklist" component={DriverOnboardingChecklistScreen} />
          <DriverStack.Screen name="DriverDocumentUpload" component={DriverDocumentUploadScreen} />
          <DriverStack.Screen name="DriverOnboardingStatus" component={DriverOnboardingStatusScreen} />
        </>
      ) : onboardingStatus !== "APPROVED" ? (
        <>
          <DriverStack.Screen name="DriverOnboardingChecklist" component={DriverOnboardingChecklistScreen} />
          <DriverStack.Screen name="DriverDocumentUpload" component={DriverDocumentUploadScreen} />
          <DriverStack.Screen name="DriverOnboardingStatus" component={DriverOnboardingStatusScreen} />
        </>
      ) : (
        <>
          <DriverStack.Screen name="DriverTabs" component={DriverTabNavigator} />
          <DriverStack.Screen name="DriverAssignmentDetail" component={DriverAssignmentDetailScreen} />
          <DriverStack.Screen name="DriverPickup" component={DriverPickupScreen} />
          <DriverStack.Screen name="DriverAwaitingStart" component={DriverAwaitingStartScreen} />
          <DriverStack.Screen name="DriverActiveTrip" component={DriverActiveTripScreen} />
          <DriverStack.Screen name="DriverTripComplete" component={DriverTripCompleteScreen} />
        </>
      )}
    </DriverStack.Navigator>
  );
}

export function RootNavigator() {
  const activeRole = useAppSelector((state) => state.session.activeRole);

  const navTheme = {
    ...DefaultTheme,
    colors: {
      ...DefaultTheme.colors,
      background: colors.background.app,
      card: colors.background.surface,
      text: colors.text.primary,
      primary: colors.primary.base,
      border: colors.border.soft
    }
  };

  const resolvedRole = env.appVariant === "dual" ? activeRole : env.appVariant === "customer" ? "CUSTOMER" : "DRIVER";

  return (
    <NavigationContainer theme={navTheme}>
      <RootStack.Navigator screenOptions={{ headerShown: false }}>
        {env.appVariant === "dual" && !resolvedRole ? (
          <RootStack.Screen name="RolePicker" component={RolePickerScreen} />
        ) : resolvedRole === "CUSTOMER" ? (
          <RootStack.Screen name="CustomerRoot" component={CustomerNavigator} />
        ) : (
          <RootStack.Screen name="DriverRoot" component={DriverNavigator} />
        )}
      </RootStack.Navigator>
    </NavigationContainer>
  );
}
