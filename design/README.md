# Handoff: حسابدار من — طرحِ جدید (سبکِ جیبک)

## Overview
Visual + interaction redesign of the Android app «حسابدار من» (Kotlin + Jetpack Compose, RTL) in a flat, cheerful, gamified style — internally "جیبک" (Jibak), after the wallet mascot. Repo: `ebrahimpersianh/Bank`, branch `claude/bank-9fiumm`, source path `native-android/app/src/main/kotlin/ir/sadteam/loancalc`.

**This supersedes the earlier "Liquid Glass" handoff.** Glass surfaces, translucent fills, glowing borders and background blur are all gone. The new language is: opaque white cards, 2px solid borders, hard offset shadows (no blur), pill buttons, one accent hue per context, Vazirmatn throughout.

## Design files — read in this order
1. **`سیستمِ طراحی جیبک.dc.html`** — the token + component reference. Start here. Every color hex, type size/weight, corner radius, shadow, spacing rule, button state, card variant, list row, bottom nav and indicator is defined once, with usage rules. This is the source of truth for values.
2. **`Duolingo Redesign.dc.html`** — the full screen library: **189 frames across 38 numbered sections**, phone mockups 336×~700–820px, each card carrying a stable id (`15a`, `28c`, `29d`, `30f`…) and a caption explaining the design decision. Section ۰ is the entry point: the five bottom-nav screens in nav order (right→left) — خانه 15a · دارایی 26b · گزارش 26a · بودجه 27c · سررسید 3a — with their empty states directly beneath (15b · 21a · 21c · 21d · 21e). Section ۲۹ holds the previously-missing screens, ۳۰ the dark variants, ۳۵ the SMS/notification permission flow, ۳۶ the ten missing controls. Many turns include an "مشخصاتِ پیاده‌سازی" block written against the actual Kotlin source. Light and dark variants sit side by side (light `Na`, dark `Nb`).
3. **`پروتوتایپِ کلیک‌خور.dc.html`** — a working clickable prototype of the core flows. Use it to resolve any question about *behavior*: what enables a button, what a tap does, what state a row lands in.
4. **`حسابدار من - بازنگری کامل.dc.html`** — the earlier all-in-one overview doc; still valid, superset of screens, lower detail than the above.

These are **design references, not production code.** Recreate them in the existing Compose codebase, reusing screen logic and lifting exact values from the HTML `style` attributes. Do not port HTML/CSS.

## Fidelity
High-fidelity for layout, spacing, color, copy and interaction. Icons in the mockups are placeholder inline SVGs (line, 2.3–2.8px stroke, round caps) — swap for the app's existing Material icon set. Persian numerals with `٬` grouping everywhere; negative amounts use `−`, never parentheses.

## Design tokens (light)
Full table in `سیستمِ طراحی جیبک.dc.html`; the load-bearing ones:

| Role | Hex |
|---|---|
| Primary green | `#0EA968` |
| Green shadow / link text | `#0B8C57` |
| Expense red | `#FF4B4B` (text `#D93838`) |
| Info blue | `#1CB0F6` |
| Budget purple | `#A56EFF` |
| Soft warning orange | `#FF9600` |
| Money/achievement card | `#FFFCF4` → `#F3E7CE`, border `#EBD9B4`, text `#8B6F3D` |
| Page background | `#F5FBFF` |
| Card surface | `#FFFFFF`, border `#E3ECE7` (list rows `#EEF3F0`) |
| Text | `#16221C` primary, `#5b6a63` secondary, `#8b9a93` label |
| Disabled | fill `#DDE7E2`, text `#94A5A0` |

Radii: 12 icon frame · 16 list row · 20 card · 28 sheet · 999 button.
Shadows: raised `0 4px 0 <darker same hue>`, neutral `0 3px 0 #E8EFEB`. Press = shadow collapses to 1–2px and the element translates down. **No blurred or colored shadows anywhere.**
Spacing: multiples of 4 — 14–16 inside cards, 11–14 between cards, 16–18 page margin. Min touch target 44px.
Type: Vazirmatn 500/700/800/900. 28/900 hero number · 18/900 page title · 12.5/900 row title · 10–11/800 button+chip · 9.5/700 row metadata.

## Dark theme — a token map, not a second design
Dark is a **pure token swap**; no layout is authored twice. The colored hues keep their meaning, only base/surface/line/text change:

| Role | Light | Dark |
|---|---|---|
| Base | `#F5FBFF` | `#10181F` |
| Surface | `#FFFFFF` | `#1B2530` |
| Surface alt | `#F7FAF8` | `#161F28` |
| Border | `#E3ECE7` | `#2A3640` |
| Border (rows) | `#EEF3F0` | `#232E38` |
| Text | `#16221C` | `#F3F7F5` |
| Text 2 | `#5b6a63` | `#9FB0AA` |
| Chip fill | `#F1F5F2` | `#232E38` |
| Green | `#0EA968` | `#17C57D` |
| Red | `#FF4B4B` | `#FF6B6B` |
| Orange | `#FF9600` | `#FFA733` |
| Purple | `#A56EFF` | `#B98CFF` |
| Gold card | `#FFFCF4`→`#F3E7CE` | `#242018`→`#1B1712`, border `#3A3226`, text `#D9C79B` |
| Disabled | `#DDE7E2`/`#94A5A0` | `#232E38`/`#6C7B75` |

Implement as a single Compose `ColorScheme` pair in `theme/Color.kt` + a `ThemeMode` preference (light/dark/system). The prototype's moon toggle demonstrates the whole swap live. Section ۳۰ of the screen library draws every light frame again in dark (60 frames) for verification; the captions there are intentionally terse because dark is a swap, not a separate design.

## Identity
One logo, one splash — turn 24 in the screen library. `24a` is the canonical mark (green wallet + banknote + coin, the mascot "جیبک"), `24b` the splash built from that same mark, `24c` the usage rules (sizes: 110 splash, 96 onboarding, 64 empty state, 38 hint bubble; monochrome and single-color variants included). Earlier logo explorations were removed from the file — 24 is the only version. The animated app-icon states (activity-driven) are the same mark, not a second logo.

Mascot animations are named in the CSS and reused by name: `jibakPopIn` (entry), `jibakFloat` (idle), `jibakCoinFeed` (coin), `jibakFadeUp` (content reveal), `jibakSheetUp` (bottom sheet), `jibakToast`. The mascot never animates at the same time as important text.

## Screens covered
Existing screens, restyled: Home, Report/Stats, Assets, Budget, Due/Installments, Loan detail + installment table, Cheque list/books/add-edit, Sayad inquiry, Financial calendar, Categories, Settings, Add/Edit transaction (sheet + full page), shared modals/pickers.

New in this redesign — **no existing code, needs product sign-off before build**:
- **Onboarding** (2 steps, ids `28a`/`28b`) — the goal picks are not decorative: they decide which cards rank first on Home. Needs a stored `UserGoals` set.
- **Phone login + 5-digit OTP** (`28c`) — SMS verification backend required. Code is 5 digits (fixed — the SMS provider pattern is approved for 5); the confirm button stays disabled until all five are entered. Data stays on-device; the number is only for backup.
- **Global search** (`28d`) — grouped results (loans/instalments, transactions, cheques) each with their own subtotal, row-level primary action inline.
- **Notification centre** (`28e`) — grouped by day, each item has exactly one action or none; read items fade and drop their dot.
- **App lock** (`28f`) — PIN keypad with biometrics inside the keypad grid, not a separate button.
- **Gamification** — streak, coins, badges/achievements. Entirely new: needs `streak`, `coins`, `badges: Map<BadgeId, UnlockedAt?>`.
- **Recurring payments** — already built and working in the app; restyle only, no new data model. Same for **counterparties (طرفِ حساب)** — the base exists.
- **Appearance + Language settings** — needs `ThemeMode` preference and i18n scaffolding.

## Interaction spec (from the prototype)
- Bottom nav: 5 tabs — خانه، دارایی، گزارش، بودجه، سررسید (fixed set; وام and چک are NOT tabs — they open from inside other screens). **No center FAB**; adding a transaction is a button inside Home. Active tab = thicker icon stroke (2.6 vs 2.3) + 900-weight green label. Due tab carries a red dot while anything is unpaid.
- Amount entry uses the app's own numeric keypad (with a `۰۰۰` key), not the system keyboard.
- Save button label doubles as state: «مبلغ را بزن» while empty, «ذخیره · <دسته>» when valid; disabled fill until valid.
- Paying an instalment settles the row in place (opacity 0.66, chip turns «پرداخت شد») and fires a toast — no navigation.
- Budget category rows expand in place for detail.
- Destructive actions always take a second confirmation.
- Copy voice: second-person singular, short sentences, no banking jargon; the number comes before the explanation.

## Assets
No external images. All icons are placeholder inline SVGs — replace with the app's existing Material icon set. The mascot/logo is pure CSS+divs in the mockups; it needs to be produced as a real vector asset before shipping.

## Files
- `سیستمِ طراحی جیبک.dc.html` — tokens + components (start here)
- `Duolingo Redesign.dc.html` — 37 sections / 189 frames, all screens, light + dark, logo/splash, live shortcut drawer
- `پروتوتایپِ کلیک‌خور.dc.html` — clickable prototype of the core flows
- `حسابدار من - بازنگری کامل.dc.html` — all-in-one overview doc
- `github.md` (project root) — repo/branch/screen-map metadata


## Coverage (final)
- 158 frames total: 98 light + 60 dark. Every screen has exactly one design, in both themes.
- Section 31 = shortcut drawer (new feature). 31a is a WORKING prototype inside the file — drag the bottom handle up, long-press a tile to reorder. 31b shows the reorder state statically, 31c is the full motion/data spec.
- Section 32 = profile avatar figures (boy/girl, 6 colors, picker sheet, size table).
- Section 33 = the loading / empty / error states for section 29, plus per-screen empty copy for all 16 screens and 5 state rules.
- Section 34 = the two remaining brand marks: the streak chain (`زنجیرِ فعال` — linked rings, broken-link state, 11px chip variant, dark variant) and the receipt ribbon (`ریبونِ رسید` — 14×10px serrated bottom edge). With the coin these are the app's three signatures; 34c states where each may and may not appear.
- Section 36 = the ten features that existed in the current app but had no redesigned screen: cheque archive + un-archive, nested categories, multi-select instalment payment, per-instalment note/receipt photo/reference number, font size, account deletion, account type + opening balance, custom date range, loan edit/delete, manual loan entry. Each frame carries a "کجا می‌نشیند" note naming the exact card and position. 36i records the structural decision that instalment payment happens ONLY from the full instalment page (29p) — card 27b intentionally has no per-row pay button. Dark twins are the "۳۶ تیره" section.
- Section 35 = the notification-listener permission flow (was gap #1): 35a first-run permission step with SMS + notifications side by side, 35b notification explainer with the bank list, 35c illustrated 3-step Android settings guide, 35d the two return states, 35e battery-optimisation and autostart traps, 35f the settings row in all three states plus rules.
- Section 29 = the 16 screens that had no design before: check report, loan stats, buy/sell asset sheet, loan calc result, calc history, ledger detail, asset detail, permission gate, Jalali date picker, loan ceiling calculator, chequebooks, cheque detail, cheque reminder, deposit, note, bank loan.
- Section 30 = dark twin of every light frame. Token map: bg #10181F · surface #1B2530 · surface2 #202B36 · chip #232E38 · line #2A3640 · text #F3F7F5 · text2 #8B9A94 · text3 #7A8A84 · green text/icon #3DDC96 · filled green #0EA968 shadow #07724A · green pill rgba(61,220,150,.14) · red #FF6B6B on rgba(255,107,107,.12) · gold #F0D9A8 on #2A2317. Sizes, spacing and radii identical to light.
- Duplicates removed (13 screens had two designs; the fuller one was kept): home 15a · report 26a · assets 26b · budget 27c · settings 27d · add transaction 17a · categories 17b · cheque list 26c · recurring payment 26d · modals 27e · search 28d · login/OTP 28c · empty states 21a-21g.

## Known gaps (deliberate — read before estimating)
1. **Section 35c needs real screenshots.** The 3-step Android guide is drawn as wireframes; replace them with real captures of the phone's "Notification access" screen — the user has to recognise what they are looking at.
2. **Google Play restricts Notification Listener access.** Cafe Bazaar / Myket are unaffected. If Play is a target, that needs a fallback path — a product decision, not a design one.
3. **The clickable prototype covers the core flows only** — not all 189 frames. Side paths are not wired.
4. **Dark-frame captions are repetitive** ("same layout as X with the dark map"). The colour work itself was re-judged — surfaces, icon inks and `0 3px 0` elevation were corrected per-case — but the captions carry little information for a developer.
5. **All imagery is placeholder.** Icons → the app's existing Material set; the mascot/logo needs a real vector before shipping.
