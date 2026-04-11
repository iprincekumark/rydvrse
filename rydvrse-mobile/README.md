# Rydvrse Mobile

## Overview

`rydvrse-mobile` is the React Native mobile workspace for the Rydvrse MVP.

It implements the mobile experience defined in:

- [Product_Requirements_Document.md](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/Product_Requirements_Document.md)
- [Screen_Flow.md](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/Screen_Flow.md)
- [Wireframes_and_UX_States.md](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/Wireframes_and_UX_States.md)
- [API_Spec.md](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/API_Spec.md)
- [Implementation_Backlog.md](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/Implementation_Backlog.md)

The workspace supports:

- `customer` app flow
- `driver` app flow
- `dual` app-variant mode for local development

## Project Structure

```text
rydvrse-mobile/
├── App.tsx
├── app.json
├── package.json
├── src/
│   ├── components/
│   ├── constants/
│   ├── navigation/
│   ├── samples/
│   ├── screens/
│   ├── services/
│   ├── store/
│   ├── theme/
│   └── utils/
└── __tests__/
```

## Design System

- Primary color: deep teal
- Secondary accent: warm amber
- Base background: warm off-white
- Font family: `Manrope`
- Design goal: calm, premium, trust-first

## Navigation

- Customer flow:
  - auth stack
  - tab shell
  - booking and trip stack screens
- Driver flow:
  - auth and onboarding stack
  - tab shell
  - assignment and trip stack screens

## API and Mock Strategy

The app uses a centralized service layer in [src/services/api](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-mobile/src/services/api).

Environment flags:

- `EXPO_PUBLIC_API_BASE_URL`
- `EXPO_PUBLIC_USE_MOCKS`
- `EXPO_PUBLIC_APP_VARIANT`

Default local behavior uses mocks so the product flows stay fully explorable even when backend seed data is incomplete.

## Commands

```bash
npm install
npm run typecheck
npm test
npm start
```

For device testing with Expo Go:

```bash
npm run start:lan
```

If Expo Go shows `Failed to download remote update`, switch to tunnel mode:

```bash
npm run start:tunnel
```

## Environment

Copy [.env.example](/Users/prince/Documents/CoDiN/Princevrse/STARTUP/wORk/rydvrse/rydvrse-mobile/.env.example) into a local `.env` file if you want to override defaults.

- `EXPO_PUBLIC_API_BASE_URL`
  Base URL for the Spring Boot backend
- `EXPO_PUBLIC_USE_MOCKS`
  `true` keeps the app explorable without backend data
- `EXPO_PUBLIC_APP_VARIANT`
  `customer`, `driver`, or `dual`

## Verification

The mobile workspace has been verified with:

- `npm run typecheck`
- `npm test`

This gives us a stable baseline for navigation, Redux state transitions, and shared component rendering before device-level QA.

## Local Device Troubleshooting

- keep `EXPO_PUBLIC_USE_MOCKS=true` during early device testing unless backend connectivity is intentionally being tested
- if a phone cannot download the dev bundle on LAN, use `npm run start:tunnel`
- keep the phone and laptop on stable internet and disable VPN or network filtering if Expo Go cannot connect
- make sure Expo Go is updated to a version compatible with Expo SDK 53

## Notes

- `dual` mode is intended for local development and design review.
- production builds can be split into separate customer and driver binaries later by changing `EXPO_PUBLIC_APP_VARIANT`.
- admin and ops remain outside this mobile workspace by design and belong in the web surface.
