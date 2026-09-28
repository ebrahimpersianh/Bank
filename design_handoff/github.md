repo: ebrahimpersianh/Bank
branch: main
path: native-android/app/src/main/kotlin/ir/sadteam/loancalc

## Last sync
date: 2026-08-11T19:05:00Z

### Updated in this project
- کل محیط برنامه با یک زبان بصری واحد (الهام از iOS) بازطراحی شد — ۲۴ صفحه
- گروه وام: وام بانکی، نتیجه و جدول اقساط، وام‌های من، جزئیات وام، افزودن دستی، توان بازپرداخت، سود سپرده، تاریخچه
- گروه چک: داشبورد چک، ثبت، جزئیات، دسته‌چک‌ها، گزارش‌دهی، استعلام صیادی
- آنبوردینگ کامل: اسپلش، گیت مجوز، امکانات، ورود، خوش‌آمد، قفل برنامه
- داده‌ی واقعی ریپو: ۳۰ بانک، ۶ سرویس اعتباری و ۷ وام پرتکرار با نرخ و مبلغ واقعی؛ واحد پول در همه صفحه‌ها ریال
- آیکون‌های خطی یکدست جای ایموجی، تم تیره/روشن، انیمیشن لمسی روی همه عناصر

## Screen map
| صفحه پروژه | فایل‌های ریپو |
| --- | --- |
| اسپلش | ui/onboarding/SplashIntroScreen.kt |
| گیت مجوز | ui/onboarding/PermissionGateScreen.kt |
| امکانات | ui/onboarding/BenefitsScreen.kt |
| ورود | ui/auth/LoginScreen.kt |
| خوش‌آمد | ui/onboarding/WelcomeScreen.kt |
| قفل برنامه | ui/security/LockScreen.kt |
| وام بانکی | ui/BankLoanScreen.kt |
| نتیجه محاسبه | ui/ResultScreen.kt |
| توان بازپرداخت | ui/AffordScreen.kt |
| سود سپرده | ui/DepositScreen.kt |
| وام‌های من | ui/myloans/MyLoansScreen.kt |
| جزئیات وام | ui/myloans/LoanDetailScreen.kt |
| افزودن وام دستی | ui/myloans/AddManualLoanScreen.kt |
| تاریخچه محاسبات | ui/history/CalculationHistoryScreen.kt |
| داشبورد چک | ui/cheque/ChequeScreen.kt |
| ثبت/ویرایش چک | ui/cheque/AddEditChequeScreen.kt |
| جزئیات چک | ui/cheque/ChequeDetailScreen.kt |
| دسته‌چک‌ها | ui/cheque/ChequeBooksScreen.kt |
| گزارش‌دهی چک | ui/cheque/ChequeReportScreen.kt |
| استعلام صیادی | ui/cheque/SayadInquiryScreen.kt |
| تقویم مالی | ui/calendar/FinancialCalendarScreen.kt |
| حساب‌ها | ui/account/AccountsScreen.kt |
| جزئیات حساب | ui/account/AccountDetailScreen.kt |
| اشتراک | ui/subscription/SubscriptionScreen.kt |
| گزارش و نمودار | ui/stats/StatsScreen.kt |
| تنظیمات | ui/settings/SettingsScreen.kt |
| بانک‌ها و وام‌های پرتکرار | data/Banks.kt, data/LoanPresets.kt |
| تم و کامپوننت‌ها | ui/theme/Color.kt, ui/theme/Theme.kt, ui/components/AppCard.kt, ui/components/GradientButton.kt |

## Sync history
- 2026-08-10 — طراحی اولیه ۸ صفحه (خانه، تراکنش، گزارش، بودجه، حساب‌ها، دسته‌بندی، تنظیمات، اسپلش)
