import React from "react";
import { StyleSheet, View } from "react-native";

import { Text } from "@/components/primitives/Text";
import { colors, space } from "@/theme";

type Step = {
  label: string;
  completed?: boolean;
  active?: boolean;
};

type ProgressStepsProps = {
  steps: Step[];
};

export function ProgressSteps({ steps }: ProgressStepsProps) {
  return (
    <View style={styles.container}>
      {steps.map((step, index) => {
        const isCompleted = step.completed;
        const isActive = step.active;
        const dotColor = isCompleted
          ? colors.state.success
          : isActive
          ? colors.brand.primary
          : colors.neutral[300];
        const textColor = isCompleted
          ? colors.state.success
          : isActive
          ? colors.brand.primary
          : colors.neutral[400];

        return (
          <View key={step.label} style={styles.stepRow}>
            <View style={styles.stepIndicator}>
              <View style={[styles.dot, { backgroundColor: dotColor }]}>
                {isCompleted ? (
                  <Text variant="caption" color={colors.neutral[0]} style={styles.checkmark}>✓</Text>
                ) : (
                  <Text variant="caption" color={colors.neutral[0]} style={styles.stepNum}>
                    {index + 1}
                  </Text>
                )}
              </View>
              {index < steps.length - 1 ? (
                <View
                  style={[
                    styles.line,
                    { backgroundColor: isCompleted ? colors.state.success : colors.neutral[200] },
                  ]}
                />
              ) : null}
            </View>
            <Text variant="bodySm" color={textColor} style={styles.label}>
              {step.label}
            </Text>
          </View>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 0,
  },
  stepRow: {
    flexDirection: "row",
    alignItems: "flex-start",
    gap: space[3],
  },
  stepIndicator: {
    alignItems: "center",
    width: 28,
  },
  dot: {
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center",
  },
  checkmark: {
    fontSize: 14,
    lineHeight: 16,
    fontWeight: "700",
  },
  stepNum: {
    fontSize: 12,
    lineHeight: 14,
    fontWeight: "700",
  },
  line: {
    width: 2,
    height: 24,
  },
  label: {
    paddingTop: 4,
    flex: 1,
  },
});
