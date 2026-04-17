# Rydvrse UI/UX Improvement Plan

## Document Control

- **Product:** Rydvrse (Ride-hailing Mobile App)
- **Version:** 1.0
- **Date:** 2026-04-17
- **Scope:** Customer App, Driver App, Admin Dashboard
- **Design Reference:** CarGo Rental Car Mobile App Design (Figma Community)
- **API Reference:** Rydvrse Swagger API v1

---

## 1. UI/UX Audit of Current System

### 1.1 What's Working Well

The existing codebase demonstrates strong engineering fundamentals. The centralized theme system at `src/theme/index.ts` provides a single source of truth for design tokens. The component hierarchy (Screen, HeaderBlock, BottomActionBar, SectionCard) shows thoughtful layout architecture. Accessibility basics are already in place via `accessibilityRole`, `accessibilityLabel`, and `accessibilityState` on interactive elements. The responsive breakpoint system using `useWindowDimensions()` covers a range from mobile (390px) to desktop (1100px), and the typography scale uses a single coherent typeface (Manrope) with well-defined semantic variants (hero, title, section, body, caption, overline).

### 1.2 Layout and Spacing Problems

**Monochrome color system lacks visual hierarchy.** The current palette is essentially grayscale: primary is `#242424`, secondary is `#898989`, and backgrounds are `#FFFFFF` and `#F5F5F5`. While this creates a clean look, it leaves no room for visual emphasis, brand identity, or emotional connection. Users cannot quickly scan a screen and distinguish between primary actions, information zones, and navigational elements. The CarGo design reference uses a rich blue primary (`#1A73E8` range) with warm accents that create instant visual hierarchy without relying on layout alone.

**Spacing scale is too compressed.** The current spacing scale runs from 4px to 40px in 8 steps (4, 8, 12, 16, 20, 24, 32, 40). The gaps between mid-range values (16 → 20 → 24) are only 4px apart, which is imperceptible on most screens. This forces developers to use the same few spacing values everywhere, creating visual monotony. The recommended scale should follow a more geometric progression with clearer visual steps.

**Screen layout is too uniform.** Every screen uses the same `Screen` wrapper with identical padding. The home screen, a booking detail screen, and an onboarding screen all feel structurally identical. High-impact screens (home, trip status, payment) should break out of the standard container with full-bleed hero sections, colored headers, or map backgrounds.

**Card component lacks visual variety.** `SectionCard` is the only card component, producing a "stacked boxes" layout everywhere. There's no visual distinction between a booking summary card, a driver info card, a promotional banner, or a settings row. This forces users to read every piece of text to understand what they're looking at.

### 1.3 Visual Hierarchy Problems

**No accent color or brand color.** The app currently has no memorable brand color. Enterprise ride-hailing apps use strong brand colors for instant recognition: Uber uses black + green, Ola uses yellow + green, Lyft uses pink. Rydvrse's `#242424` primary is functionally black, which is generic and forgettable. The CarGo design uses a deep blue with white text on colored surfaces, creating strong contrast and brand presence.

**Buttons are visually monotone.** The `PrimaryButton` comes in two variants: dark filled and light outlined. Both use the same 54px height, same border radius (8px), and same font weight. There's no visual distinction between a "Confirm Booking" button (high-stakes, irreversible) and a "View Details" button (low-stakes, navigational). The CarGo design uses larger, more rounded buttons with gradient fills for primary actions and ghost/text buttons for secondary actions.

**Status indicators are text-heavy.** The `StatusChip` component uses colored backgrounds with text labels, which is functional but not scannable. The current approach requires users to read "CONFIRMED" or "PENDING" rather than recognizing a visual pattern. Adding iconography to status states (checkmark for confirmed, clock for pending, alert for issues) would make status scanning instant.

### 1.4 Missing Modern UX Patterns

The following patterns are absent from the current implementation and are standard in production ride-hailing apps:

- **Pull-to-refresh** on list screens (bookings, earnings, jobs)
- **Swipe gestures** on cards (swipe to call driver, swipe to cancel)
- **Bottom sheet modals** for contextual actions (instead of full-screen navigation)
- **Haptic feedback** on confirmations and critical actions
- **Animated transitions** between screens (currently uses default React Navigation transitions)
- **Map integration** as a first-class citizen (currently uses `MapPlaceholderCard`)
- **Real-time updates** via SSE/WebSocket for trip tracking (API supports SSE at `/trips/{tripId}/tracking/stream`)
- **Onboarding walkthrough** with illustration-driven slides
- **Biometric authentication** for returning users
- **Dark mode** support
- **Skeleton loading states** exist but are underutilized (only the `Skeleton` component exists, not screen-level skeleton layouts)

### 1.5 Responsiveness Gaps

The responsive system uses width-based breakpoints but doesn't address orientation changes, dynamic type sizes, or safe area variations across devices. The bottom tab bar uses a fixed 76px height regardless of device, which feels oversized on compact phones and undersized on tablets.

---

## 2. Figma Design Breakdown (CarGo Rental Car App)

### 2.1 Design Philosophy

The CarGo design follows a "Premium Minimal" aesthetic: clean surfaces with purposeful use of color, generous whitespace, and high-contrast interactive elements. It balances visual richness with usability, using photographic content (car images) as the primary visual anchor and reserving color for actions and status.

### 2.2 Recommended Color System (Adapted from CarGo for Rydvrse)

```
Primary Palette:
  brand:        #1B6EF3   (vibrant blue - primary brand color)
  brandStrong:  #1558C9   (pressed/active state)
  brandSoft:    #EBF2FE   (light tint for backgrounds)
  brandSubtle:  #F5F8FF   (very light tint for surfaces)

Neutral Palette:
  gray900:      #1A1D21   (headings, primary text)
  gray700:      #3D4350   (secondary text, labels)
  gray500:      #6B7280   (placeholder text, muted)
  gray300:      #D1D5DB   (borders, dividers)
  gray100:      #F3F4F6   (muted backgrounds)
  gray50:       #F9FAFB   (elevated surfaces)
  white:        #FFFFFF   (base surface)

Semantic Palette:
  success:      #059669   (confirmations, completed)
  successSoft:  #ECFDF5
  warning:      #D97706   (delays, cautions)
  warningSoft:  #FFFBEB
  danger:       #DC2626   (errors, cancellations, SOS)
  dangerSoft:   #FEF2F2
  info:         #2563EB   (informational, in-progress)
  infoSoft:     #EFF6FF

Accent (optional for differentiation):
  accent:       #F59E0B   (amber - promotions, highlights)
  accentSoft:   #FEF3C7
```

### 2.3 Typography System

The CarGo design uses a combination of a geometric sans-serif for headings and a humanist sans for body text. Adapting this for Rydvrse while keeping Manrope (which is excellent):

```
Display:
  displayLg:    { size: 32, weight: 800, lineHeight: 38, tracking: -0.8 }
  displaySm:    { size: 26, weight: 800, lineHeight: 32, tracking: -0.5 }

Heading:
  headingLg:    { size: 22, weight: 700, lineHeight: 28, tracking: -0.3 }
  headingSm:    { size: 18, weight: 700, lineHeight: 24, tracking: -0.15 }

Body:
  bodyLg:       { size: 16, weight: 500, lineHeight: 24 }
  bodyMd:       { size: 15, weight: 500, lineHeight: 22 }
  bodySm:       { size: 14, weight: 500, lineHeight: 20 }
  bodyStrong:   { size: 15, weight: 600, lineHeight: 22 }

Utility:
  caption:      { size: 12, weight: 500, lineHeight: 16, color: gray500 }
  overline:     { size: 11, weight: 600, lineHeight: 16, tracking: 0.8, uppercase }
  label:        { size: 14, weight: 600, lineHeight: 20 }
  tabLabel:     { size: 11, weight: 600, lineHeight: 14 }
```

### 2.4 Spacing Scale (Revised)

Replace the current compressed scale with a geometric progression that produces clearer visual steps:

```
space-1:    4px    (inline element padding, icon gaps)
space-2:    8px    (tight element spacing, chip padding)
space-3:    12px   (form field inner padding, list item gaps)
space-4:    16px   (standard component padding, section gaps)
space-5:    20px   (card padding, screen horizontal margin)
space-6:    24px   (section separation)
space-7:    32px   (major section breaks)
space-8:    40px   (screen vertical padding)
space-9:    48px   (hero section padding)
space-10:   64px   (screen-level breathing room)
```

### 2.5 Component Patterns Extracted from CarGo

**Buttons:**
- Primary: 52px height, 12px border-radius, brand blue fill, white text, bold weight. Pressed state darkens 15%. Includes optional leading/trailing icon.
- Secondary: 52px height, 12px border-radius, white fill, 1px gray300 border, gray900 text. Pressed state: brandSoft background.
- Ghost: No background or border, brand blue text, used inline.
- Danger: Same as primary but danger red fill. Used only for destructive actions (cancel booking, SOS).
- Icon Button: 44px circle, gray100 background, centered icon. Used for back navigation, close, and contextual actions.

**Cards:**
- Surface Card: white background, 1px gray200 border, 16px border-radius, 16px padding, subtle shadow (0 1px 3px rgba(0,0,0,0.06)). Used for general content grouping.
- Elevated Card: white background, no border, 16px border-radius, stronger shadow (0 4px 12px rgba(0,0,0,0.08)). Used for booking summaries and actionable items.
- Feature Card: brand gradient background (brandSoft → white), 16px border-radius, no border. Used for promotional content and CTAs on the home screen.
- Driver Card: horizontal layout with avatar, name/rating/vehicle on the left, call/chat icons on the right. 60px avatar with 30px rounded corners.

**Navigation:**
- Bottom Tab Bar: 64px height + safe area, white background, top border (1px gray200), 5 items max. Active state: brand blue icon + label. Inactive: gray500 icon + label. No background highlight on active tab.
- Top Navigation: transparent or white, back arrow (24px), centered title (headingSm), optional right-side action button. Status bar: dark content on light screens, light content on dark/colored headers.
- Bottom Sheet: 50% to 90% screen height, 24px top border-radius, 40px drag handle centered at top, backdrop blur (rgba(0,0,0,0.3)). Replaces full-screen modals for actions like "Select Service Type", "Cancel Booking", "Rate Trip".

**Forms:**
- Text Input: 52px height, 12px border-radius, 1px gray300 border, 16px horizontal padding. Focus state: 2px brand blue border. Error state: danger red border + error message below. Label above (label style, gray700). Optional left icon (20px, gray500).
- Selector: Same as text input but with right chevron icon, non-editable, opens bottom sheet.
- Date/Time Picker: Native picker wrapped in bottom sheet with confirm/cancel buttons.

**Status Indicators:**
- Chip: Rounded pill (999px radius), 8px vertical + 12px horizontal padding, colored background + matching text. Each state has its own background/text pair from the semantic palette.
- Banner: Full-width, 12px border-radius, 12px padding, left-aligned icon + text. Same semantic coloring as chips but larger and more prominent.
- Progress Steps: Horizontal dots or vertical timeline. Active step uses brand blue, completed uses success green with checkmark, upcoming uses gray300.

**Maps:**
- Map takes up 40-60% of screen on location-centric screens (pickup, trip tracking).
- Overlaid UI sits at the bottom in a bottom sheet that can be dragged up.
- Route shown with brand blue polyline, 4px width. Pickup and drop markers use custom SVG pins in brand colors.

### 2.6 Design Tokens Summary

```typescript
// Proposed theme/tokens.ts structure
export const tokens = {
  color: { /* as defined in 2.2 */ },
  space: { /* as defined in 2.4 */ },
  radius: {
    none: 0,
    sm: 6,
    md: 12,
    lg: 16,
    xl: 20,
    full: 9999
  },
  shadow: {
    sm: { offset: [0, 1], blur: 3, color: 'rgba(0,0,0,0.06)' },
    md: { offset: [0, 4], blur: 12, color: 'rgba(0,0,0,0.08)' },
    lg: { offset: [0, 8], blur: 24, color: 'rgba(0,0,0,0.10)' },
    xl: { offset: [0, 16], blur: 40, color: 'rgba(0,0,0,0.12)' }
  },
  animation: {
    fast: 150,
    normal: 250,
    slow: 400,
    spring: { damping: 20, stiffness: 180 }
  }
}
```

---

## 3. Component Architecture (Frontend)

### 3.1 Current Problems

The biggest architectural issue is that `CustomerScreens.tsx` is a single 40KB+ file containing all customer screens, inline components, and business logic. This is unmaintainable, untestable, and prevents code splitting. The same pattern likely exists in `DriverScreens.tsx`. Individual screens should be separate files with their own concerns.

### 3.2 Proposed Folder Structure

```
src/
├── assets/
│   ├── brand/              (BrandMark, BrandLockup — keep as-is)
│   ├── icons/              (AppIcon, ServiceIcon — keep as-is)
│   ├── illustrations/      (EmptyStateArtwork, HeaderArtwork — keep as-is)
│   └── images/             (NEW: static images, car photos, map assets)
│
├── components/
│   ├── primitives/         (NEW: atomic-level components)
│   │   ├── Text.tsx           (replaces AppText, adds all variants)
│   │   ├── Button.tsx         (Primary, Secondary, Ghost, Danger, Icon variants)
│   │   ├── IconButton.tsx     (circular icon-only button)
│   │   ├── Chip.tsx           (status chips with icons)
│   │   ├── Badge.tsx          (notification dots, counts)
│   │   ├── Divider.tsx        (horizontal rule with optional label)
│   │   ├── Spacer.tsx         (declarative spacing)
│   │   └── Avatar.tsx         (driver/customer avatars with fallback)
│   │
│   ├── inputs/             (NEW: form-specific components)
│   │   ├── TextField.tsx      (enhanced from existing)
│   │   ├── SearchField.tsx    (with clear button, suggestions)
│   │   ├── PhoneInput.tsx     (country code + number for OTP)
│   │   ├── OtpInput.tsx       (individual digit boxes)
│   │   ├── LocationPicker.tsx (address search with map preview)
│   │   ├── DateTimePicker.tsx (bottom sheet date/time selection)
│   │   ├── Selector.tsx       (dropdown-like, opens bottom sheet)
│   │   └── StarRating.tsx     (interactive rating input)
│   │
│   ├── cards/              (EXPANDED: purpose-specific cards)
│   │   ├── SurfaceCard.tsx    (generic container — replaces SectionCard)
│   │   ├── BookingCard.tsx    (extracted from CustomerScreens)
│   │   ├── DriverInfoCard.tsx (avatar, name, rating, vehicle, CTA)
│   │   ├── QuoteSummaryCard.tsx (fare breakdown display)
│   │   ├── ServiceTypeCard.tsx  (replaces ChoiceCard for services)
│   │   ├── EarningCard.tsx    (driver earnings display)
│   │   ├── OfferCard.tsx      (job offer for drivers)
│   │   ├── TripCard.tsx       (active trip summary)
│   │   └── PromoCard.tsx      (promotional/feature highlight)
│   │
│   ├── layout/             (ENHANCED: structural components)
│   │   ├── Screen.tsx         (enhanced with variant props)
│   │   ├── Header.tsx         (replaces HeaderBlock, adds color variants)
│   │   ├── BottomBar.tsx      (replaces BottomActionBar)
│   │   ├── BottomSheet.tsx    (NEW: draggable modal sheet)
│   │   ├── KeyboardAvoid.tsx  (NEW: keyboard-aware wrapper)
│   │   ├── Section.tsx        (NEW: titled section with optional CTA)
│   │   └── Row.tsx            (NEW: horizontal layout helper)
│   │
│   ├── feedback/           (NEW: user feedback components)
│   │   ├── Skeleton.tsx       (enhanced with preset shapes)
│   │   ├── ScreenSkeleton.tsx (NEW: full-screen skeleton layouts)
│   │   ├── Toast.tsx          (NEW: non-blocking notifications)
│   │   ├── EmptyState.tsx     (enhanced from existing)
│   │   ├── ErrorState.tsx     (NEW: error with retry)
│   │   ├── Banner.tsx         (replaces StatusBanner, adds dismiss)
│   │   └── ProgressSteps.tsx  (NEW: multi-step progress indicator)
│   │
│   ├── maps/               (NEW: map-related components)
│   │   ├── MapView.tsx        (wrapper around react-native-maps)
│   │   ├── RouteOverlay.tsx   (pickup → drop polyline)
│   │   ├── LocationMarker.tsx (custom pin component)
│   │   └── MapBottomSheet.tsx (map + bottom sheet combo layout)
│   │
│   └── patterns/           (NEW: composed multi-component patterns)
│       ├── BookingFlow.tsx    (step indicator + content + action bar)
│       ├── TripTimeline.tsx   (vertical timeline: booked → assigned → pickup → drop)
│       ├── FareBreakdown.tsx  (itemized fare table)
│       ├── SignalStrip.tsx    (trust indicators — extracted)
│       └── ContactBar.tsx     (call + chat + SOS action bar)
│
├── screens/
│   ├── customer/
│   │   ├── auth/
│   │   │   ├── LoginScreen.tsx
│   │   │   ├── OtpScreen.tsx
│   │   │   └── ProfileSetupScreen.tsx
│   │   ├── home/
│   │   │   ├── HomeScreen.tsx
│   │   │   └── ServiceSetupScreen.tsx
│   │   ├── booking/
│   │   │   ├── QuoteScreen.tsx
│   │   │   ├── BookingReviewScreen.tsx
│   │   │   ├── BookingStatusScreen.tsx
│   │   │   ├── BookingDetailScreen.tsx
│   │   │   └── BookingListScreen.tsx
│   │   ├── trip/
│   │   │   ├── AssignedDriverScreen.tsx
│   │   │   ├── StartTripScreen.tsx
│   │   │   ├── ActiveTripScreen.tsx
│   │   │   └── TripCompleteScreen.tsx
│   │   ├── payment/
│   │   │   ├── PaymentScreen.tsx
│   │   │   └── InvoiceScreen.tsx
│   │   ├── support/
│   │   │   ├── SupportHomeScreen.tsx
│   │   │   ├── CreateTicketScreen.tsx
│   │   │   └── TicketDetailScreen.tsx
│   │   └── profile/
│   │       ├── ProfileScreen.tsx
│   │       ├── SavedLocationsScreen.tsx
│   │       └── SettingsScreen.tsx
│   │
│   ├── driver/
│   │   ├── auth/
│   │   │   ├── LoginScreen.tsx
│   │   │   └── OtpScreen.tsx
│   │   ├── onboarding/
│   │   │   ├── OnboardingFormScreen.tsx
│   │   │   ├── DocumentUploadScreen.tsx
│   │   │   └── ApprovalStatusScreen.tsx
│   │   ├── home/
│   │   │   └── DashboardScreen.tsx
│   │   ├── jobs/
│   │   │   ├── OffersScreen.tsx
│   │   │   ├── AssignmentDetailScreen.tsx
│   │   │   └── UpcomingJobsScreen.tsx
│   │   ├── trip/
│   │   │   ├── PickupScreen.tsx
│   │   │   ├── AwaitingStartScreen.tsx
│   │   │   ├── ActiveTripScreen.tsx
│   │   │   └── TripCompleteScreen.tsx
│   │   ├── earnings/
│   │   │   ├── EarningsSummaryScreen.tsx
│   │   │   └── LedgerScreen.tsx
│   │   ├── support/
│   │   │   ├── SupportHomeScreen.tsx
│   │   │   └── CreateTicketScreen.tsx
│   │   └── profile/
│   │       ├── ProfileScreen.tsx
│   │       └── DocumentsScreen.tsx
│   │
│   └── shared/
│       ├── RolePickerScreen.tsx
│       └── OnboardingWalkthrough.tsx   (NEW)
│
├── hooks/                  (NEW: custom hooks)
│   ├── useApi.ts              (generic fetch + loading/error state)
│   ├── useAuth.ts             (login/logout/token refresh)
│   ├── useBooking.ts          (booking CRUD operations)
│   ├── useTrip.ts             (trip tracking + SSE stream)
│   ├── useLocation.ts         (device GPS)
│   ├── useBottomSheet.ts      (bottom sheet state management)
│   ├── useRefresh.ts          (pull-to-refresh logic)
│   └── useHaptic.ts           (haptic feedback wrapper)
│
├── navigation/
│   ├── RootNavigator.tsx
│   ├── CustomerNavigator.tsx  (extracted from monolith)
│   ├── DriverNavigator.tsx    (extracted from monolith)
│   └── types.ts
│
├── services/
│   └── api/                (keep existing structure, enhance)
│       ├── client.ts
│       ├── auth.ts
│       ├── customer.ts
│       ├── driver.ts
│       ├── admin.ts        (NEW: admin API module)
│       └── types.ts
│
├── store/                  (keep Redux Toolkit, add async)
│   ├── index.ts
│   ├── sessionSlice.ts
│   ├── customerSlice.ts
│   ├── driverSlice.ts
│   └── middleware/
│       └── apiMiddleware.ts   (NEW: centralized error handling)
│
├── theme/
│   ├── tokens.ts           (NEW: raw token values)
│   ├── index.ts            (re-export composed theme)
│   └── darkTheme.ts        (NEW: dark mode overrides)
│
└── utils/
    ├── format.ts           (keep existing)
    ├── validation.ts       (NEW: form validation helpers)
    ├── storage.ts          (NEW: AsyncStorage wrapper)
    └── haptics.ts          (NEW: haptic feedback utilities)
```

### 3.3 Key Architectural Decisions

**Split monolith screen files.** The most impactful refactor is breaking `CustomerScreens.tsx` and `DriverScreens.tsx` into individual screen files. Each screen becomes its own file with its own styles and concerns. Shared sub-components (like `BookingCard`, `SectionTitle`) get extracted to the `components/` directory.

**Component composition over configuration.** Instead of one `PrimaryButton` with boolean flags (`secondary`, `disabled`, `danger`), create a `Button` component with a `variant` prop: `<Button variant="primary">`, `<Button variant="ghost">`, `<Button variant="danger">`. This is more readable and extensible.

**Custom hooks for API integration.** Instead of calling API functions directly in screen components and managing loading/error states manually in each screen, create custom hooks like `useBooking()` that encapsulate the API call, loading state, error handling, and data caching. This eliminates duplicated loading/error state boilerplate across screens.

**Bottom sheet as a first-class pattern.** Many flows that currently navigate to full-screen modals (service selection, cancellation confirmation, rating) should use bottom sheets instead. This keeps the user's context visible and feels more native.

---

## 4. API Integration Strategy

### 4.1 API-to-Screen Mapping

| Screen | API Endpoints | Data Flow |
|--------|---------------|-----------|
| **Customer Home** | `GET /customers/me/home`, `GET /bookings` | Fetch on mount + pull-to-refresh. Show upcoming bookings, active trip banner, service grid. |
| **Login / OTP** | `POST /auth/otp/request`, `POST /auth/otp/verify` | Two-step flow. Step 1: phone → OTP request. Step 2: OTP code → verify → hydrate session. |
| **Profile Setup** | `PATCH /customers/me` | One-time setup after first login. Collect name, email, city. |
| **Service Setup** | `POST /config/serviceability/check` | Validate pickup/drop locations are in service area before allowing quote creation. |
| **Quote** | `POST /quotes`, `GET /quotes/{quoteId}` | Create quote → show fare breakdown. Poll or re-fetch if quote approaches expiry. |
| **Booking Review** | `POST /bookings` | Confirm booking from quote. Requires Idempotency-Key. Show confirmation animation on success. |
| **Booking Status** | `GET /bookings/{bookingId}` | Poll every 15s during assignment phase. Show progress steps. |
| **Assigned Driver** | `GET /bookings/{bookingId}`, `GET /trips/{tripId}` | Show driver details from booking response. Transition to trip tracking when trip starts. |
| **Active Trip** | `GET /trips/{tripId}/tracking/stream` (SSE) | Real-time location updates via Server-Sent Events. Show map with moving marker. |
| **Trip Start Confirmation** | `POST /bookings/{bookingId}/start-confirmation` | Customer confirms trip start. Required before billing begins. |
| **Payment** | `POST /bookings/{bookingId}/payment-orders` | Create Razorpay/payment session. Handle success/failure webhooks. |
| **Rating** | `POST /bookings/{bookingId}/ratings` | Star rating + optional tags + comment. Show after trip completion. |
| **Invoice** | `GET /bookings/{bookingId}/invoice` | Download/display receipt after payment. |
| **Booking List** | `GET /bookings` | Paginated list with status filters. Pull-to-refresh. |
| **Booking Detail** | `GET /bookings/{bookingId}` | Full booking info with timeline, fare, driver details. |
| **Booking Cancel** | `GET /bookings/{bookingId}/cancellation-preview`, `POST /bookings/{bookingId}/cancel` | Two-step: preview penalty → confirm cancel. |
| **Booking Modify** | `POST /bookings/{bookingId}/modification-preview`, `POST /bookings/{bookingId}/modify` | Two-step: preview price change → confirm modification. |
| **Support** | `GET /support/tickets`, `POST /support/tickets`, `GET /support/tickets/{ticketId}` | List tickets, create new ticket with category/severity, view details. |
| **Saved Locations** | `GET/POST/PATCH/DELETE /customers/me/saved-locations` | CRUD for favorite addresses. |
| **SOS** | `POST /trips/{tripId}/sos` | Emergency alert during active trip. Immediate action, no confirmation dialog. |
| **Trip Share** | `POST /trips/{tripId}/share-links` | Generate shareable tracking link. Copy to clipboard or share via native share sheet. |
| **Driver Dashboard** | `GET /drivers/me/dashboard` | Earnings summary, availability toggle, pending offers. |
| **Driver Offers** | `GET /drivers/assignments/offers`, `POST .../accept`, `POST .../decline` | List offers with accept/decline. Idempotency-Key on actions. |
| **Driver Trip Flow** | `POST /drivers/trips/{tripId}/arrived`, `POST .../complete`, `POST .../location-pings` | Sequential trip lifecycle. Location pings sent every 5-10 seconds during active trip. |
| **Driver Earnings** | `GET /drivers/earnings/summary`, `GET /drivers/earnings/ledger` | Period-filtered summary + transaction list. |
| **Driver Documents** | `GET /drivers/documents`, `POST /drivers/documents` | List uploaded docs, add new with type/media/expiry. |
| **Driver Availability** | `PATCH /drivers/me/availability` | Toggle online/offline with reason code for going offline. |
| **App Bootstrap** | `GET /config/bootstrap` | Fetch on app launch. Returns service types, cities, feature flags, minimum app version. Cache locally. |

### 4.2 State Management Strategy

**Keep Redux Toolkit** for global state (session, user profile, active booking/trip). It's already well-structured and the team is familiar with it.

**Add React Query (TanStack Query)** for server state management. This is the single highest-impact improvement for data flow:

```
npm install @tanstack/react-query
```

React Query handles caching, background refetching, stale data management, loading/error states, retry logic, and optimistic updates. This eliminates the need for manual `useState` + `useEffect` + try/catch patterns in every screen.

**Recommended data ownership:**

| Layer | Owns | Examples |
|-------|------|---------|
| Redux | Client state | Auth tokens, active role, UI preferences, booking form draft |
| React Query | Server state | Bookings list, trip details, driver profile, earnings data |
| Local state | Ephemeral UI state | Form inputs, bottom sheet open/closed, animation progress |

### 4.3 Loading, Error, and Empty States

Every API-connected screen must implement all three states:

**Loading States:**
- Use `ScreenSkeleton` components that mirror the actual screen layout (not a centered spinner).
- Skeleton shapes should match the content they're replacing: rectangle for text, circle for avatar, rounded rect for card.
- Animate with shimmer effect (already implemented in `Skeleton.tsx` using LinearGradient).
- For inline loading (e.g., "Accept Offer" button), use a spinner inside the button and disable interactions.

**Error States:**
- Use `ErrorState` component with illustration, error message, and "Retry" button.
- Distinguish between network errors ("No internet connection") and server errors ("Something went wrong").
- For form submission errors, show inline error messages on specific fields, not just a top-level alert.
- Network errors should offer a retry action. Auth errors (401) should redirect to login.

**Empty States:**
- Use `EmptyState` component with contextual illustration, message, and optional CTA.
- Examples: "No bookings yet — Book your first ride" with a "Book Now" button; "No offers right now — New jobs will appear here when available."
- Never show a blank screen or an empty scrollable area.

---

## 5. Responsiveness and Mobile-First Strategy

### 5.1 Breakpoints

```
compact:    0 - 389px     (small phones: iPhone SE, older Androids)
standard:   390 - 430px   (standard phones: iPhone 14/15/16, Pixel)
large:      431 - 640px   (large phones: iPhone Pro Max, Galaxy Ultra)
tablet:     641 - 1024px  (tablets: iPad Mini, iPad, Android tablets)
desktop:    1025+         (web view if applicable)
```

### 5.2 Layout Behavior by Breakpoint

**Compact (0-389px):**
- Single column layout
- Reduce horizontal padding to 16px
- Stack BottomActionBar buttons vertically
- Use smaller font sizes for hero text (28px instead of 32px)
- Hide non-essential UI elements (trust signal strip)

**Standard (390-430px):**
- Default design target. All components optimized for this range
- 20px horizontal padding
- Full component display

**Large (431-640px):**
- Same as standard but with slightly more breathing room
- Cards can show slightly more content (full addresses instead of truncated)

**Tablet (641-1024px):**
- Two-column layouts where applicable (booking list: list left, detail right)
- Max content width of 680px
- Larger map views
- Side-by-side service cards (2 per row instead of stacked)

**Desktop (1025px+):**
- Max content width of 920px, centered
- Three-column grids for service selection
- Persistent sidebar navigation (replacing bottom tabs)

### 5.3 Touch-Friendly UX Improvements

- All tappable targets minimum 44x44px (currently some are smaller)
- Increase bottom tab touch targets to include 8px padding around icons
- Add press feedback (opacity change + subtle scale transform) on all interactive elements
- Implement swipe-to-go-back on iOS with native feel
- Add long-press on booking cards for quick actions (copy booking ID, share, cancel)
- Spacing between adjacent tappable elements minimum 8px to prevent mis-taps

---

## 6. UX Enhancements

### 6.1 Micro-interactions

**Button Press Feedback:**
The current button press effect (`scale: 0.985, opacity: 0.92`) is too subtle to feel responsive. Implement a spring-based press animation:
```
Pressed:  scale(0.96), opacity(0.85) with spring timing (damping: 15, stiffness: 150)
Released: scale(1.0), opacity(1.0) with spring timing (damping: 20, stiffness: 200)
```

**Toggle Switch (Driver Availability):**
When the driver toggles availability, animate the switch with a color transition (gray → green for available, green → gray for unavailable) and trigger a light haptic pulse. If going offline requires a reason, slide in a bottom sheet after the toggle animation completes.

**Pull-to-Refresh:**
Use a custom refresh indicator that shows the Rydvrse brand mark (or a stylized car icon) instead of the default system spinner. The icon rotates while loading.

**Booking Confirmation:**
After successful booking creation, show a 1.5-second success animation: a checkmark that draws itself (Lottie or SVG animation), the fare amount scales up from 0, and the booking ID fades in. This creates a moment of delight at a high-emotional-value point.

**Tab Bar Active State:**
When switching tabs, the active icon should have a brief scale-up animation (1.0 → 1.15 → 1.0) with a subtle background dot or pill that fades in behind it.

### 6.2 Screen Transitions

**Stack Navigation:**
Use `cardStyleInterpolator: CardStyleInterpolators.forHorizontalIOS` for standard push/pop transitions. This is more natural than the default fade.

**Modal Presentations:**
Bottom sheets slide up from the bottom with a spring animation. Full-screen modals (trip tracking, payment) use a vertical slide-up with backdrop dimming.

**Booking Flow Transitions:**
Within the booking flow (service selection → quote → review → confirm), use a shared element transition on the fare amount card. The fare "travels" from one screen to the next, creating visual continuity.

**Tab Switching:**
Cross-fade between tabs with a 200ms duration instead of the default instant swap. This prevents the jarring "flash" of content replacement.

### 6.3 Skeleton Loaders

Create purpose-built skeleton screens for each major screen type:

**Home Screen Skeleton:**
- Rectangle for header area (full width, 120px)
- 2x small squares for service cards
- 3x card-shaped rectangles for booking list
- All with shimmer animation

**Booking Detail Skeleton:**
- Map placeholder (full width, 200px)
- Timeline skeleton (3 dots with connecting lines)
- Card rectangles for fare breakdown and driver info

**Earnings Skeleton:**
- Large number placeholder (centered)
- Period selector placeholder
- 5x list item rectangles for transaction history

### 6.4 Toast Notifications

Add a non-blocking toast system for background events:
- Booking assigned: "Your driver is on the way!" (success tone)
- Driver arrived: "Your driver has arrived" (info tone)
- Payment received: "Payment of ₹1,250 confirmed" (success tone)
- Quote expiring: "Your quote expires in 2 minutes" (warning tone)

Toasts should appear at the top of the screen, below the status bar, auto-dismiss after 4 seconds, and be swipeable to dismiss.

---

## 7. Production-Level Improvements

### 7.1 Accessibility

**Current State:** Good foundation with `accessibilityRole` and `accessibilityLabel` on buttons. Needs expansion.

**Required Improvements:**

Contrast Ratios: The current `#898989` secondary text on `#FFFFFF` background fails WCAG AA (contrast ratio 3.5:1, minimum is 4.5:1). The proposed `#6B7280` (gray500) achieves 5.0:1. All text colors in the new palette should be verified against WCAG AA standards.

Screen Reader: Add `accessibilityHint` to buttons that have non-obvious outcomes. For example, "Accept Offer" should have hint "Double tap to accept this job and notify the customer." Add `accessibilityLiveRegion="polite"` to status banners and trip status updates so screen readers announce changes.

Keyboard Navigation: Ensure all interactive elements are reachable via Tab key (relevant for web builds and external keyboards on tablets). Add visible focus indicators (2px brand blue outline) on focused elements.

Reduced Motion: Check `AccessibilityInfo.isReduceMotionEnabled()` and disable spring animations, replacing them with simple opacity fades. This respects users who experience motion sickness.

Dynamic Type: Support iOS Dynamic Type and Android font scaling. Set `allowFontScaling={true}` on all text components. Test all screens at 200% font scale to ensure nothing breaks.

Touch Targets: Audit all tappable elements for 44x44px minimum. The current `StatusChip` and `KeyValueRow` components may fall below this threshold.

### 7.2 Performance Optimization

**List Virtualization:** Replace `ScrollView` with `FlatList` for all list screens (bookings, earnings ledger, support tickets, driver offers). `FlatList` only renders visible items, which is critical for lists that can grow to hundreds of items.

**Image Optimization:** When car images, driver avatars, or map tiles are added, use `expo-image` (which supports caching, progressive loading, and blur-up placeholders) instead of React Native's `Image` component.

**Code Splitting:** Split customer and driver navigators into separate bundles. Since users are either customers or drivers (not both simultaneously), the inactive role's code never needs to load.

**Memoization:** Wrap expensive components (map views, fare breakdowns, long lists) in `React.memo()`. Use `useMemo` for computed values (formatted currencies, filtered lists) and `useCallback` for event handlers passed as props.

**Bundle Size:** The current app ships with mock data (`src/samples/mockData.ts`) in production. Gate this behind `__DEV__` or the `EXPO_PUBLIC_USE_MOCKS` flag to tree-shake it from production builds.

**Startup Time:** Load the bootstrap config (`GET /config/bootstrap`) during the splash screen phase. Cache it in AsyncStorage so returning users see content immediately while the app refreshes in the background.

### 7.3 Design Consistency Enforcement

**Lint Rules:** Add ESLint rules that forbid inline colors (`color: "#FF0000"`) and inline font sizes. All visual values must come from the theme tokens.

**Component Library Documentation:** Create a Storybook-style component catalog (using `@storybook/react-native` or a simple in-app debug screen) that shows every component in every state. This serves as a living design spec.

**Design Review Checklist:** Before merging any PR that touches UI, verify against this checklist:
1. Uses theme tokens (no hardcoded colors, spacing, or font sizes)
2. Implements loading, error, and empty states
3. Passes WCAG AA contrast check
4. Minimum 44x44px touch targets
5. Looks correct at compact (375px) and tablet (768px) widths
6. Has `accessibilityRole` and `accessibilityLabel` on interactive elements
7. Animations respect reduced motion preferences

---

## 8. Tech Stack Recommendations

### 8.1 UI Libraries to Add

| Library | Purpose | Why |
|---------|---------|-----|
| `@gorhom/bottom-sheet` | Bottom sheet modals | Best-in-class bottom sheet for React Native. Handles gestures, snap points, keyboard avoidance, and backdrop. |
| `react-native-reanimated` | Animations | 60fps animations on the UI thread. Required by bottom-sheet and enables spring physics, shared element transitions. |
| `@tanstack/react-query` | Server state | Caching, background refetching, retry logic, optimistic updates. Eliminates manual loading/error state management. |
| `react-native-maps` | Map views | Google Maps / Apple Maps integration. Required for trip tracking, pickup/drop selection. |
| `expo-haptics` | Haptic feedback | Light/medium/heavy haptic pulses for confirmations, errors, toggles. |
| `expo-image` | Image optimization | Caching, progressive loading, blur-up placeholders, WebP support. |
| `react-native-toast-message` | Toast notifications | Non-blocking notifications for background events. |
| `lottie-react-native` | Complex animations | Booking confirmation, onboarding walkthrough, empty state illustrations. |
| `@react-native-async-storage/async-storage` | Persistent storage | Cache bootstrap config, auth tokens, user preferences locally. |
| `react-native-mmkv` | Fast key-value storage | Alternative to AsyncStorage for performance-critical reads (auth tokens, feature flags). Synchronous API. |
| `expo-secure-store` | Secure credential storage | Store auth tokens securely in the device keychain/keystore. |

### 8.2 Libraries NOT Recommended

| Library | Why Not |
|---------|---------|
| NativeWind / Tailwind | The app already has a well-structured StyleSheet system. Switching to Tailwind would require rewriting all styles and add a build step. |
| Styled Components | Adds runtime overhead on React Native. StyleSheet is more performant. |
| UI Kits (NativeBase, React Native Paper) | These impose their own design system. Rydvrse already has a custom design language; using a kit would create visual conflicts. |
| MobX / Zustand | Redux Toolkit is already in place and working well. Switching state management has no clear benefit. |

### 8.3 Styling Approach

**Keep React Native StyleSheet** as the primary styling method. It's performant (styles are sent to native once) and the team is already using it well.

**Enhance the theme system** with the new token structure. Create a `useTheme()` hook that returns the active theme (light or dark), enabling dark mode support without refactoring components:

```typescript
// Usage in components
const { colors, spacing } = useTheme();
```

**Add a `styled()` utility** for common patterns:

```typescript
// Lightweight style composition helper
const Card = styled(View, (theme) => ({
  backgroundColor: theme.colors.white,
  borderRadius: theme.radius.lg,
  padding: theme.space[5],
  ...theme.shadow.md,
}));
```

This is not styled-components — it's a thin wrapper that generates StyleSheets at build time.

---

## 9. Step-by-Step Execution Plan

### Phase 1: Foundation (Week 1-2)

**Goal:** Establish the design system and component architecture without breaking existing functionality.

| Day | Task | Impact | Effort |
|-----|------|--------|--------|
| 1-2 | **Upgrade theme tokens.** Replace `src/theme/index.ts` with the new color palette, spacing scale, radius, shadow, and typography tokens. Update `colors`, `spacing`, `radius`, `shadows`, and `typography` exports to use the new values. Run the app — everything will look different but functionally work. | High | Low |
| 3 | **Add React Query.** Install `@tanstack/react-query`, create a `QueryClientProvider` in `App.tsx`. Don't migrate any API calls yet — just establish the infrastructure. | Medium | Low |
| 4 | **Add `@gorhom/bottom-sheet` and `react-native-reanimated`.** Install, configure the Reanimated Babel plugin, add `BottomSheet` component wrapper. Test with a simple bottom sheet on one screen. | Medium | Low |
| 5-6 | **Refactor primitives.** Create new `Button` (with variants), `Text`, `IconButton`, `Chip`, `Avatar`, `Divider`, `Spacer` components using new tokens. Keep old components temporarily — new ones coexist. | High | Medium |
| 7-8 | **Refactor layout components.** Upgrade `Screen`, `Header`, `BottomBar`. Add `Section`, `Row`, `KeyboardAvoid` components. | High | Medium |
| 9-10 | **Split CustomerScreens.tsx.** Extract each screen into its own file under `src/screens/customer/`. This is pure file reorganization with import path updates. Run all existing tests to verify. | Critical | Medium |

### Phase 2: Customer App Visual Overhaul (Week 3-4)

**Goal:** Transform the customer experience with the new design language.

| Day | Task | Impact | Effort |
|-----|------|--------|--------|
| 11-12 | **Redesign Home Screen.** Full-bleed brand header with greeting and location. Service type cards in a 2x2 grid with icons and labels. Active booking banner (if exists). Upcoming bookings list. Trust signal strip. Pull-to-refresh. Migrate from direct API calls to React Query `useQuery`. | Very High | High |
| 13-14 | **Redesign Login and OTP Flow.** Full-screen brand illustration at top. Phone input with country code. OTP input with individual digit boxes and auto-advance. Animated success state. | High | Medium |
| 15-16 | **Redesign Booking Flow.** Service setup → Quote → Review → Confirm as a multi-step flow with progress indicator. Bottom sheet for service type selection. Animated fare card. Map preview for pickup/drop locations. | Very High | High |
| 17-18 | **Redesign Booking Status and Active Trip.** Real-time map with driver location (SSE integration). Bottom sheet with trip details, driver info, and contact actions. Progress timeline (Booked → Assigned → En route → Arrived → Trip started → Completed). | Very High | High |
| 19-20 | **Redesign Booking List and Detail.** FlatList with pull-to-refresh. Filter chips (All, Upcoming, Completed, Cancelled). BookingCard with service icon, status chip, fare, and date. Detail screen with full timeline, fare breakdown, driver card, and action buttons. | High | Medium |

### Phase 3: Driver App and Feedback Systems (Week 5-6)

**Goal:** Transform the driver experience and implement all feedback patterns.

| Day | Task | Impact | Effort |
|-----|------|--------|--------|
| 21-22 | **Redesign Driver Dashboard.** Earnings summary card (today's earnings, total rides). Availability toggle with animated state change. Pending offers section with offer cards. Quick stats strip. | High | Medium |
| 23-24 | **Redesign Driver Trip Flow.** Map-centric screens for pickup, active trip, and completion. Location pings integration. Arrival notification. Trip completion with earnings summary. | High | High |
| 25-26 | **Implement Skeleton Loaders.** Create skeleton variants for Home, Booking List, Booking Detail, Driver Dashboard, Earnings. Replace all loading spinners with content-shaped skeletons. | High | Medium |
| 27-28 | **Implement Error and Empty States.** Create `ErrorState` and enhance `EmptyState` for every list/detail screen. Add retry logic. Add network status detection. | High | Medium |
| 29-30 | **Implement Toast System.** Add `react-native-toast-message` with custom styled toasts. Wire up toasts for booking confirmations, driver assignments, payment confirmations, and errors. | Medium | Low |

### Phase 4: Polish and Production Readiness (Week 7-8)

**Goal:** Animations, accessibility, performance, and dark mode.

| Day | Task | Impact | Effort |
|-----|------|--------|--------|
| 31-32 | **Add Animations.** Booking confirmation animation (Lottie). Button spring press. Tab switch cross-fade. Bottom sheet gestures. Status change transitions. | High | Medium |
| 33-34 | **Accessibility Audit.** Fix contrast ratios. Add `accessibilityHint` to all buttons. Add `accessibilityLiveRegion` to dynamic content. Test with VoiceOver (iOS) and TalkBack (Android). Support reduced motion. | Critical | Medium |
| 35-36 | **Performance Optimization.** Replace ScrollViews with FlatLists. Add React.memo to expensive components. Implement code splitting between customer/driver. Cache bootstrap config. Remove mock data from production bundle. | High | Medium |
| 37-38 | **Dark Mode.** Create `darkTheme.ts` with inverted color scheme. Add theme context with system preference detection. Test all screens in dark mode. | Medium | High |
| 39-40 | **Final QA and Polish.** Cross-device testing (iPhone SE, iPhone 16, Pixel 8, iPad). Fix spacing inconsistencies. Final animation timing tweaks. Performance profiling. | Critical | Medium |

### Priority Summary

If time is limited, this is the priority order for maximum impact:

1. **Theme tokens upgrade** (Day 1-2) — Instantly transforms visual identity
2. **Split monolith screen files** (Day 9-10) — Unlocks all future work
3. **Home screen redesign** (Day 11-12) — First impression matters most
4. **Booking flow redesign** (Day 15-18) — Core user journey
5. **React Query migration** (Day 3 + ongoing) — Eliminates loading/error bugs
6. **Skeleton loaders** (Day 25-26) — Perceived performance boost
7. **Accessibility fixes** (Day 33-34) — Legal compliance + user reach

---

## 10. Bonus: Unique UI Ideas

### 10.1 "Ride Confidence Score"

Instead of just showing a driver's star rating (which every ride-hailing app does), show a "Confidence Score" that combines multiple signals into a single visual:

The score is a circular gauge (0-100) that fills with brand blue, showing: driver rating weight (40%), on-time percentage (30%), and number of completed trips (30%). Below the gauge, show the three contributing factors as small bar charts. This gives customers a richer, more trustworthy signal than a simple "4.8 stars" and differentiates Rydvrse from competitors.

### 10.2 "Trip Diary" with Auto-Generated Summaries

After each trip, automatically generate a summary card that includes a map snapshot of the route, the fare breakdown, trip duration, and a one-line AI-generated description ("25-minute ride from Indiranagar to HSR Layout via Silk Board"). These cards accumulate in a "Trip Diary" section that users can scroll through like a travel journal. This turns transactional booking history into an engaging, nostalgic experience. Users can share individual trip diary cards on social media with branded templates.

### 10.3 "Smart Schedule" with Context-Aware Booking

Instead of the standard "Schedule a Ride" flow, offer a "Smart Schedule" feature that integrates with the user's calendar (optional, with explicit permission). The home screen shows a contextual prompt: "You have a meeting at JP Nagar at 3:00 PM — book a ride for 2:15 PM?" When the user taps, the booking form is pre-filled with pickup (saved home location), drop (meeting location), and time (calculated for arrival with buffer). This makes Rydvrse feel proactive rather than reactive, and creates a genuine utility moat that competitors would need deep platform integration to replicate.

---

## Appendix A: New Theme Implementation

Below is the production-ready replacement for `src/theme/index.ts`:

```typescript
import { Platform, TextStyle, ViewStyle } from "react-native";

// ─── Color Tokens ────────────────────────────────────────────────────
export const colors = {
  brand: {
    primary:    "#1B6EF3",
    strong:     "#1558C9",
    soft:       "#EBF2FE",
    subtle:     "#F5F8FF",
  },
  neutral: {
    900: "#1A1D21",
    700: "#3D4350",
    500: "#6B7280",
    400: "#9CA3AF",
    300: "#D1D5DB",
    200: "#E5E7EB",
    100: "#F3F4F6",
    50:  "#F9FAFB",
    0:   "#FFFFFF",
  },
  state: {
    success:     "#059669",
    successSoft: "#ECFDF5",
    warning:     "#D97706",
    warningSoft: "#FFFBEB",
    danger:      "#DC2626",
    dangerSoft:  "#FEF2F2",
    info:        "#2563EB",
    infoSoft:    "#EFF6FF",
  },
  accent: {
    primary:  "#F59E0B",
    soft:     "#FEF3C7",
  },
};

// ─── Semantic Aliases ────────────────────────────────────────────────
export const semantic = {
  text: {
    primary:   colors.neutral[900],
    secondary: colors.neutral[700],
    muted:     colors.neutral[500],
    inverted:  colors.neutral[0],
    brand:     colors.brand.primary,
  },
  bg: {
    app:      colors.neutral[0],
    surface:  colors.neutral[0],
    muted:    colors.neutral[100],
    elevated: colors.neutral[50],
    brand:    colors.brand.primary,
    brandSoft: colors.brand.soft,
  },
  border: {
    soft:   colors.neutral[200],
    strong: colors.neutral[300],
    brand:  colors.brand.primary,
  },
};

// ─── Spacing Scale ───────────────────────────────────────────────────
export const space = {
  1:  4,
  2:  8,
  3:  12,
  4:  16,
  5:  20,
  6:  24,
  7:  32,
  8:  40,
  9:  48,
  10: 64,
} as const;

// ─── Border Radius ───────────────────────────────────────────────────
export const radius = {
  none: 0,
  sm:   6,
  md:   12,
  lg:   16,
  xl:   20,
  full: 9999,
} as const;

// ─── Shadows ─────────────────────────────────────────────────────────
export const shadows = {
  sm: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.06,
      shadowRadius: 3,
      shadowOffset: { width: 0, height: 1 },
    },
    android: { elevation: 1 },
    default: {},
  }),
  md: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.08,
      shadowRadius: 12,
      shadowOffset: { width: 0, height: 4 },
    },
    android: { elevation: 3 },
    default: {},
  }),
  lg: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.10,
      shadowRadius: 24,
      shadowOffset: { width: 0, height: 8 },
    },
    android: { elevation: 5 },
    default: {},
  }),
  xl: Platform.select<ViewStyle>({
    ios: {
      shadowColor: "#000000",
      shadowOpacity: 0.12,
      shadowRadius: 40,
      shadowOffset: { width: 0, height: 16 },
    },
    android: { elevation: 8 },
    default: {},
  }),
};

// ─── Typography ──────────────────────────────────────────────────────
export const fontFamily = {
  medium:    "Manrope_500Medium",
  semiBold:  "Manrope_600SemiBold",
  bold:      "Manrope_700Bold",
  extraBold: "Manrope_800ExtraBold",
} as const;

export const textStyle = {
  displayLg: {
    fontFamily: fontFamily.extraBold,
    fontSize: 32,
    lineHeight: 38,
    letterSpacing: -0.8,
    color: semantic.text.primary,
  } satisfies TextStyle,
  displaySm: {
    fontFamily: fontFamily.extraBold,
    fontSize: 26,
    lineHeight: 32,
    letterSpacing: -0.5,
    color: semantic.text.primary,
  } satisfies TextStyle,
  headingLg: {
    fontFamily: fontFamily.bold,
    fontSize: 22,
    lineHeight: 28,
    letterSpacing: -0.3,
    color: semantic.text.primary,
  } satisfies TextStyle,
  headingSm: {
    fontFamily: fontFamily.bold,
    fontSize: 18,
    lineHeight: 24,
    letterSpacing: -0.15,
    color: semantic.text.primary,
  } satisfies TextStyle,
  bodyLg: {
    fontFamily: fontFamily.medium,
    fontSize: 16,
    lineHeight: 24,
    color: semantic.text.secondary,
  } satisfies TextStyle,
  bodyMd: {
    fontFamily: fontFamily.medium,
    fontSize: 15,
    lineHeight: 22,
    color: semantic.text.secondary,
  } satisfies TextStyle,
  bodySm: {
    fontFamily: fontFamily.medium,
    fontSize: 14,
    lineHeight: 20,
    color: semantic.text.secondary,
  } satisfies TextStyle,
  bodyStrong: {
    fontFamily: fontFamily.semiBold,
    fontSize: 15,
    lineHeight: 22,
    letterSpacing: -0.05,
    color: semantic.text.primary,
  } satisfies TextStyle,
  caption: {
    fontFamily: fontFamily.medium,
    fontSize: 12,
    lineHeight: 16,
    color: semantic.text.muted,
  } satisfies TextStyle,
  overline: {
    fontFamily: fontFamily.semiBold,
    fontSize: 11,
    lineHeight: 16,
    letterSpacing: 0.8,
    textTransform: "uppercase",
    color: semantic.text.muted,
  } satisfies TextStyle,
  label: {
    fontFamily: fontFamily.semiBold,
    fontSize: 14,
    lineHeight: 20,
    color: semantic.text.primary,
  } satisfies TextStyle,
  tabLabel: {
    fontFamily: fontFamily.semiBold,
    fontSize: 11,
    lineHeight: 14,
  } satisfies TextStyle,
};

// ─── Animation Tokens ────────────────────────────────────────────────
export const animation = {
  fast:   150,
  normal: 250,
  slow:   400,
  spring: { damping: 20, stiffness: 180 },
};

// ─── Aggregated Theme ────────────────────────────────────────────────
export const theme = {
  colors,
  semantic,
  space,
  radius,
  shadows,
  fontFamily,
  textStyle,
  animation,
};

// Backward-compatible aliases (remove after migration)
export const spacing = {
  xxs: space[1],
  xs:  space[2],
  sm:  space[3],
  md:  space[4],
  lg:  space[5],
  xl:  space[6],
  xxl: space[7],
  xxxl: space[8],
};

export const typography = {
  family: fontFamily,
  text: {
    hero:       textStyle.displayLg,
    title:      textStyle.displaySm,
    section:    textStyle.headingSm,
    overline:   textStyle.overline,
    body:       textStyle.bodyMd,
    bodyStrong: textStyle.bodyStrong,
    caption:    textStyle.caption,
  },
};
```

---

## Appendix B: Package Installation Commands

```bash
# Phase 1 — Foundation
npm install @gorhom/bottom-sheet react-native-reanimated react-native-gesture-handler
npm install @tanstack/react-query
npm install @react-native-async-storage/async-storage

# Phase 2 — Maps and Media
npm install react-native-maps
npm install expo-image
npm install expo-haptics

# Phase 3 — Polish
npm install lottie-react-native
npm install react-native-toast-message
npm install react-native-mmkv
npm install expo-secure-store

# Babel config addition for Reanimated
# Add 'react-native-reanimated/plugin' to babel.config.js plugins array
```

---

## Appendix C: Migration Checklist

Use this checklist when migrating each screen from the old design to the new:

- [ ] Screen extracted to its own file (not in monolith)
- [ ] Uses new theme tokens (colors, space, radius, shadows, textStyle)
- [ ] No inline color/spacing values
- [ ] Loading state uses ScreenSkeleton
- [ ] Error state uses ErrorState with retry
- [ ] Empty state uses EmptyState with CTA
- [ ] All buttons use new Button component with variant prop
- [ ] Cards use appropriate card type (SurfaceCard, BookingCard, etc.)
- [ ] Lists use FlatList (not ScrollView) for dynamic content
- [ ] Pull-to-refresh implemented on list screens
- [ ] AccessibilityRole, accessibilityLabel, and accessibilityHint set
- [ ] Touch targets minimum 44x44px
- [ ] Tested at 375px and 768px widths
- [ ] Tested in dark mode
- [ ] Tested with VoiceOver/TalkBack
- [ ] Animations respect reduced motion preference
- [ ] API calls use React Query hooks
- [ ] Haptic feedback on confirmations and errors
