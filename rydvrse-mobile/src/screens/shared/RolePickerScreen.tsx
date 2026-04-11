import React from "react";
import { View } from "react-native";

import { ChoiceCard } from "@/components/common/ChoiceCard";
import { HeaderBlock } from "@/components/layout/HeaderBlock";
import { Screen } from "@/components/layout/Screen";
import { useAppDispatch } from "@/store";
import { switchRole } from "@/store/sessionSlice";
import { spacing } from "@/theme";

export function RolePickerScreen() {
  const dispatch = useAppDispatch();

  return (
    <Screen>
      <HeaderBlock
        eyebrow="Rydvrse"
        title="Choose the experience you want to open."
        subtitle="The same mobile workspace can run the customer or driver flow while we keep one shared design system."
      />

      <View style={{ gap: spacing.md, marginTop: spacing.xxl }}>
        <ChoiceCard
          title="Customer app"
          subtitle="Book a verified driver, track the ride, pay clearly, and raise support issues with full trip context."
          onPress={() => dispatch(switchRole("CUSTOMER"))}
        />
        <ChoiceCard
          title="Driver app"
          subtitle="Handle onboarding, availability, job offers, trip execution, earnings, and support from one task-first flow."
          onPress={() => dispatch(switchRole("DRIVER"))}
        />
      </View>
    </Screen>
  );
}
