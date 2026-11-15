repo: ebrahimpersianh/Bank
branch: claude/bank-9fiumm
path: native-android/app/src/main/kotlin/ir/sadteam/loancalc

## Last sync
date: 2026-08-21T00:00:00Z

### Updated in this project
- Rebuilt the handoff package around the current "جیبک" design (flat, gamified) and retired the Liquid Glass handoff text.
- Screen library now runs to 28 turns: added onboarding, phone login + 6-digit OTP, global search, notification centre, app lock.
- Consolidated identity to a single logo + splash (turn 24); removed earlier logo explorations.
- Added a token/component reference doc and a clickable prototype whose theme toggle demonstrates the dark-mode token map.

## Screen map
| Screen | Repo files |
|---|---|
| Home (خانه) | ui/HomeScreen (theme/Color.kt, components/AppCard.kt) |
| Report (گزارش) | ui/stats/* |
| Assets (دارایی) | ui/asset/* |
| Budget (بودجه) | ui/budget/* |
| Due / Installments (سررسید) | ui/due/*, ui/loan/* |
| Cheques (چک) | ui/cheque/* (list, books, add-edit, Sayad inquiry) |
| Calendar (تقویم مالی) | ui/calendar/* |
| Categories (دسته‌ها) | ui/category/CategoryManagementScreen.kt |
| Settings (تنظیمات) | ui/settings/SettingsScreen.kt (+ new ThemeMode pref) |
| Add/Edit transaction | ui/transaction/NewTransactionSheet.kt |
| Onboarding, login/OTP, search, notifications, app lock | no existing code — new |
| Dark theme | theme/Color.kt (single ColorScheme pair) |

## Sync history
### 2026-08-20T01:01:24Z
- Read CLAUDE.md, Color.kt, AppCard.kt to ground the "Duolingo-style" flat/cheerful direction (separate from Liquid Glass).
- Built first-look mockups for Home, Report, Assets in `Duolingo Redesign.dc.html`.
- Flagged: new palette needs new Color.kt tokens, AppCard needs a parallel flat variant, streak/achievement logic is entirely new.
