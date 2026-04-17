import React, { useRef, useState } from "react";
import { StyleSheet, TextInput, View } from "react-native";

import { colors, fontFamily, radius, semantic, space } from "@/theme";

type OtpInputProps = {
  length?: number;
  value: string;
  onChangeText: (value: string) => void;
};

export function OtpInput({ length = 6, value, onChangeText }: OtpInputProps) {
  const inputRefs = useRef<Array<TextInput | null>>([]);
  const [focusedIndex, setFocusedIndex] = useState(-1);

  const digits = value.padEnd(length, "").split("").slice(0, length);

  const handleChange = (text: string, index: number) => {
    const newDigits = [...digits];
    newDigits[index] = text.slice(-1);
    const newValue = newDigits.join("").trim();
    onChangeText(newValue);

    if (text && index < length - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleKeyPress = (e: { nativeEvent: { key: string } }, index: number) => {
    if (e.nativeEvent.key === "Backspace" && !digits[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  return (
    <View style={styles.container}>
      {Array.from({ length }, (_, i) => (
        <TextInput
          key={i}
          ref={(ref) => { inputRefs.current[i] = ref; }}
          value={digits[i] || ""}
          onChangeText={(text) => handleChange(text, i)}
          onKeyPress={(e) => handleKeyPress(e, i)}
          onFocus={() => setFocusedIndex(i)}
          onBlur={() => setFocusedIndex(-1)}
          keyboardType="numeric"
          maxLength={1}
          style={[
            styles.box,
            focusedIndex === i && styles.boxFocused,
            digits[i] ? styles.boxFilled : null,
          ]}
          accessibilityLabel={`Digit ${i + 1}`}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    gap: space[2],
    justifyContent: "center",
  },
  box: {
    width: 48,
    height: 56,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: semantic.border.soft,
    backgroundColor: semantic.bg.surface,
    textAlign: "center",
    fontSize: 22,
    fontFamily: fontFamily.bold,
    color: semantic.text.primary,
  },
  boxFocused: {
    borderWidth: 2,
    borderColor: colors.brand.primary,
  },
  boxFilled: {
    borderColor: colors.brand.primary,
    backgroundColor: colors.brand.subtle,
  },
});
