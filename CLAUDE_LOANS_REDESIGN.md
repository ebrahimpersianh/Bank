# Jibak — Loans Screen Redesign Handoff

## Target branch
ONLY: claude/bank-9fiumm

Do not touch main. Do not merge or rebase unrelated branches.

## Scope
Redesign the "وام" / My Loans list screen to match the approved visual direction from the latest design reference.

This is a UI redesign only. Preserve all existing loan data, ViewModel logic, navigation, filtering, sorting, payment behavior, auth/subscription gates, backup/sync behavior, and accessibility semantics unless a small UI wiring change is strictly required.

Do not invent new financial calculations.

## Approved visual direction

### 1. Header
- Keep the existing RTL page title: وام
- Keep the back arrow.
- Keep search as a compact icon button at the upper-left.
- Do not add a large persistent search field.
- Keep the header height compact.

### 2. Top tabs
Three equal-width tabs:
- وام‌های من — selected
- سپرده
- محاسبه‌گر

Selected tab:
- filled primary/brand green
- white text
- rounded pill/card
- subtle elevation

Unselected tabs:
- transparent / very light surface
- muted text
- no heavy border

Use the existing app theme tokens instead of introducing arbitrary hard-coded colors.

### 3. Main summary / hero card
Replace the current visually busy red/blue hero treatment with a calmer, premium financial summary card.

Content must remain data-driven:
- current/next installment title
- installment amount
- paid installment count / total installments
- payment progress percentage
- total remaining balance
- remaining months / time-to-freedom
- overdue amount
- monthly installment amount where already available

Visual:
- rounded large card
- restrained green + blue brand gradient or layered surfaces
- no giant decorative illustration
- optional extremely subtle geometric/leaf texture only if it can be implemented with Compose primitives
- circular progress ring on the left
- compact timeline/progress indicator
- high contrast for the main amount
- avoid excessive gradients, glow, or decorative noise

Important: this is a reusable Compose card, not a bitmap background.

### 4. Filter/sort row
Immediately below the hero:
- compact فیلتر control with filter icon
- segmented filter: همه / فعال / تسویه‌شده
- Keep current filtering semantics exactly as implemented.
- Keep current sort options and custom drag ordering.
- Make the controls fit in one compact row on typical phones.
- Avoid the current oversized/visually heavy filter treatment.

### 5. Loan cards
Redesign every loan row as a clean white/light-surface rounded card.

Each card should clearly expose:
- loan name
- status chip (e.g. معوق / در جریان)
- overdue amount when applicable
- bank name
- installment count/progress
- due-date information
- payment progress ring
- پرداخت button when payment is available
- chevron for opening details

Visual hierarchy:
1. loan name + status
2. important financial state / overdue amount
3. bank + installment metadata
4. progress ring
5. payment CTA

Overdue cards may use restrained danger red:
- red only for overdue state and amount
- do not tint the entire card pink/red
- no large red borders

Normal/active cards should use the app's normal surface and green/blue progress colors.

The payment button must remain an actual Compose button/action, not part of an image.

### 6. Remove/reduce visual clutter
Do not carry forward:
- large floating calculator button over the list
- excessive card outlines
- heavy red background cards
- decorative graphics that compete with financial information
- unnecessary duplicate labels

The calculator action already exists in the product flow; preserve its navigation behavior without a visually dominant floating overlay unless the current product rules require it.

### 7. Bottom navigation
Do not redesign the global bottom navigation as part of this task.
Keep the existing app navigation and selected وام state.

### 8. Responsive behavior
Must work on:
- small Android phones
- standard 360–430dp widths
- RTL Persian text
- long loan names
- large monetary values

Use AutoShrinkText, TextOverflow, weights, and responsive layout where already available.

Do not rely on fixed pixel coordinates from the reference image.

## Existing implementation to preserve

Primary screen:
native-android/app/src/main/kotlin/ir/sadteam/loancalc/ui/myloans/MyLoansScreen.kt

Related:
- MyLoansViewModel.kt
- LoanDetailScreen.kt
- AddManualLoanScreen.kt
- existing shared UI components under ui/components/
- existing theme tokens under ui/theme/

The current screen already contains important behavior that must not be lost:
- Room-backed loan list
- active/settled/all filtering
- search
- sorting options
- custom drag ordering
- overdue calculation
- monthly installment calculation
- current-month settled behavior
- loan locking/auth/subscription gates
- pull-to-refresh/sync
- deep-link opening of a specific loan
- add/edit/delete flows
- loan detail transition
- backup/restore
- app tour target callbacks
- privacy mode
- bottom-bar visibility callback

## Component guidance

Prefer extracting visual-only sections instead of making MyLoansScreen.kt even larger.

Suggested composables:
- LoansHeader
- LoansTopTabs
- LoansSummaryCard
- LoansFilterRow
- LoanListCard

Keep business logic in the existing ViewModel/screen state. Visual composables should receive already-derived values and callbacks.

Do not create a second source of truth for loan state.

## Data / calculation rule
Do not change formulas or persistence as part of this redesign.

Existing helper/data functions should remain authoritative, including:
- overdue amount/count
- current installment
- settled detection
- next due date
- progress
- filtering/sorting

If a value is missing from the current UI, derive it from existing ViewModel/state rather than introducing a duplicate calculation.

## Theme
Prefer:
- AppPrimary
- AppPrimaryDim
- AppPrimaryInk
- AppDanger
- AppMuted
- AppSurface
- AppSurface2
- AppText
- AppRadius
- AppElevation
- Motion

Do not introduce a new color system.

## Acceptance criteria

1. Screen visually follows the approved reference direction.
2. No bitmap/mockup is used as the UI.
3. All visible financial values remain live/data-driven.
4. Payment buttons remain functional.
5. Tapping a loan still opens LoanDetailScreen.
6. Search/filter/sort/custom ordering still work.
7. Active/settled/all semantics are unchanged.
8. Auth/subscription locking remains unchanged.
9. Pull-to-refresh remains functional.
10. Deep-link loan opening remains functional.
11. Privacy mode still masks sensitive values.
12. RTL layout works at 360dp width without clipping.
13. Long Persian loan names do not break the layout.
14. No changes to database schema, financial formulas, or sync contracts.
15. Do not modify main.
16. Do not build unless explicitly requested by the user.

## Important product decision
The user explicitly approved the visual direction shown in the latest redesign reference. Treat that image as the design target, but implement it using native Compose components and the existing Jibak design system.

The goal is not pixel-perfect imitation of the screenshot; the goal is a polished, maintainable, data-driven Jibak implementation with the same visual hierarchy and interaction model.
