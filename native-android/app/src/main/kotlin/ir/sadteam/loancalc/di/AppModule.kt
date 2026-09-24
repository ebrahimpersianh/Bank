package ir.sadteam.loancalc.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.AssetRepository
import ir.sadteam.loancalc.data.AttachmentStorage
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.BadgeEvaluator
import ir.sadteam.loancalc.data.CalculationHistoryRepository
import ir.sadteam.loancalc.data.CategoryRepository
import ir.sadteam.loancalc.data.ChequeRepository
import ir.sadteam.loancalc.data.CrashRepository
import ir.sadteam.loancalc.data.DangRepository
import ir.sadteam.loancalc.data.DebtRepository
import ir.sadteam.loancalc.data.GamificationRepository
import ir.sadteam.loancalc.data.IncomeRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.NoteRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.data.SavingsGoalRepository
import ir.sadteam.loancalc.data.db.AccountDao
import ir.sadteam.loancalc.data.db.AccountTransactionDao
import ir.sadteam.loancalc.data.db.AchievementDao
import ir.sadteam.loancalc.data.InboxRepository
import ir.sadteam.loancalc.data.db.InboxMessageDao
import ir.sadteam.loancalc.data.db.AppDatabase
import ir.sadteam.loancalc.data.WealthSnapshotRepository
import ir.sadteam.loancalc.data.db.AssetDao
import ir.sadteam.loancalc.data.db.WealthSnapshotDao
import ir.sadteam.loancalc.data.db.AssetTradeDao
import ir.sadteam.loancalc.data.db.BudgetDao
import ir.sadteam.loancalc.data.db.CalculationHistoryDao
import ir.sadteam.loancalc.data.db.CategoryDao
import ir.sadteam.loancalc.data.db.ChequeBookDao
import ir.sadteam.loancalc.data.db.ChequeDao
import ir.sadteam.loancalc.data.db.CoinDao
import ir.sadteam.loancalc.data.db.CounterpartyDao
import ir.sadteam.loancalc.data.db.DangEventDao
import ir.sadteam.loancalc.data.db.DangItemDao
import ir.sadteam.loancalc.data.db.DangItemShareDao
import ir.sadteam.loancalc.data.db.DangParticipantDao
import ir.sadteam.loancalc.data.db.DebtDao
import ir.sadteam.loancalc.data.db.IncomeDao
import ir.sadteam.loancalc.data.db.LoanDao
import ir.sadteam.loancalc.data.db.LoanRowDao
import ir.sadteam.loancalc.data.db.NoteDao
import ir.sadteam.loancalc.data.db.ParsingRuleDao
import ir.sadteam.loancalc.data.db.SavingsGoalDao
import ir.sadteam.loancalc.data.db.RecurringPaymentDao
import ir.sadteam.loancalc.data.network.ApiClient
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.prefs.AuthPrefs
import ir.sadteam.loancalc.data.prefs.SecurityPrefs
import ir.sadteam.loancalc.data.prefs.UiPrefs
import javax.inject.Singleton

/**
 * لایه‌ی داده (:data) عمداً از Hilt/هر فریم‌ورک DI بی‌خبره؛ سیم‌کشی وابستگی‌ها همینجا تو :app
 * انجام می‌شه، طوری که :data بتونه بدون تغییر تو پروژه‌های دیگه (یا با DI متفاوت) هم استفاده بشه.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideLoanDao(database: AppDatabase): LoanDao = database.loanDao()

    @Provides
    fun provideLoanRowDao(database: AppDatabase): LoanRowDao = database.loanRowDao()

    @Provides
    @Singleton
    fun provideLoanRepository(
        loanDao: LoanDao,
        loanRowDao: LoanRowDao,
        apiService: ApiService,
        database: AppDatabase,
        uiPrefs: UiPrefs,
    ): LoanRepository = LoanRepository(loanDao, loanRowDao, apiService, database, uiPrefs)

    @Provides
    @Singleton
    fun provideAuthPrefs(@ApplicationContext context: Context): AuthPrefs = AuthPrefs(context)

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiClient.create()

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService, authPrefs: AuthPrefs): AuthRepository =
        AuthRepository(apiService, authPrefs)

    @Provides
    @Singleton
    fun provideUiPrefs(@ApplicationContext context: Context): UiPrefs = UiPrefs(context)

    @Provides
    @Singleton
    fun provideCrashRepository(apiService: ApiService, authPrefs: AuthPrefs): CrashRepository =
        CrashRepository(apiService, authPrefs)

    @Provides
    @Singleton
    fun provideSecurityPrefs(@ApplicationContext context: Context): SecurityPrefs = SecurityPrefs(context)

    @Provides
    fun provideIncomeDao(database: AppDatabase): IncomeDao = database.incomeDao()

    @Provides
    @Singleton
    fun provideIncomeRepository(incomeDao: IncomeDao): IncomeRepository = IncomeRepository(incomeDao)

    @Provides
    fun provideChequeDao(database: AppDatabase): ChequeDao = database.chequeDao()

    @Provides
    fun provideChequeBookDao(database: AppDatabase): ChequeBookDao = database.chequeBookDao()

    @Provides
    @Singleton
    fun provideChequeRepository(
        chequeDao: ChequeDao,
        chequeBookDao: ChequeBookDao,
        apiService: ApiService,
        database: AppDatabase,
        uiPrefs: UiPrefs,
    ): ChequeRepository = ChequeRepository(chequeDao, chequeBookDao, apiService, database, uiPrefs)

    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideAccountTransactionDao(database: AppDatabase): AccountTransactionDao = database.accountTransactionDao()

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideRecurringPaymentDao(database: AppDatabase): RecurringPaymentDao = database.recurringPaymentDao()

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        transactionDao: AccountTransactionDao,
        apiService: ApiService,
        budgetDao: BudgetDao,
        recurringPaymentDao: RecurringPaymentDao,
        gamification: GamificationRepository,
        database: AppDatabase,
        uiPrefs: UiPrefs,
    ): AccountRepository =
        AccountRepository(accountDao, transactionDao, apiService, budgetDao, recurringPaymentDao, gamification, database, uiPrefs)

    @Provides
    fun provideWealthSnapshotDao(database: AppDatabase): WealthSnapshotDao = database.wealthSnapshotDao()

    @Provides
    @Singleton
    fun provideWealthSnapshotRepository(dao: WealthSnapshotDao): WealthSnapshotRepository =
        WealthSnapshotRepository(dao)

    @Provides
    fun provideAssetDao(database: AppDatabase): AssetDao = database.assetDao()

    @Provides
    fun provideAssetTradeDao(database: AppDatabase): AssetTradeDao = database.assetTradeDao()

    @Provides
    @Singleton
    fun provideAssetRepository(
        assetDao: AssetDao,
        assetTradeDao: AssetTradeDao,
        apiService: ApiService,
    ): AssetRepository = AssetRepository(assetDao, assetTradeDao, apiService)

    @Provides
    @Singleton
    fun provideAttachmentStorage(@ApplicationContext context: Context): AttachmentStorage =
        AttachmentStorage(context)

    @Provides
    fun provideCalculationHistoryDao(database: AppDatabase): CalculationHistoryDao =
        database.calculationHistoryDao()

    @Provides
    @Singleton
    fun provideCalculationHistoryRepository(dao: CalculationHistoryDao): CalculationHistoryRepository =
        CalculationHistoryRepository(dao)

    @Provides
    fun provideCounterpartyDao(database: AppDatabase): CounterpartyDao = database.counterpartyDao()

    @Provides
    fun provideDebtDao(database: AppDatabase): DebtDao = database.debtDao()

    @Provides
    @Singleton
    fun provideDebtRepository(counterpartyDao: CounterpartyDao, debtDao: DebtDao): DebtRepository =
        DebtRepository(counterpartyDao, debtDao)

    @Provides
    fun provideDangEventDao(database: AppDatabase): DangEventDao = database.dangEventDao()

    @Provides
    fun provideDangParticipantDao(database: AppDatabase): DangParticipantDao = database.dangParticipantDao()

    @Provides
    fun provideDangItemDao(database: AppDatabase): DangItemDao = database.dangItemDao()

    @Provides
    fun provideDangItemShareDao(database: AppDatabase): DangItemShareDao = database.dangItemShareDao()

    @Provides
    @Singleton
    fun provideDangRepository(
        eventDao: DangEventDao,
        participantDao: DangParticipantDao,
        itemDao: DangItemDao,
        itemShareDao: DangItemShareDao,
    ): DangRepository = DangRepository(eventDao, participantDao, itemDao, itemShareDao)

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository = NoteRepository(noteDao)

    // گیمیفیکیشن (سکه + نشانِ «فعال») - رجوع کن به GamificationRepository.
    @Provides
    fun provideCoinDao(database: AppDatabase): CoinDao = database.coinDao()

    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()

    // مرکزِ پیام‌ها (بخشِ ۴۰).
    // ⚠️ InboxRepository قبلاً `@Inject constructor`/`@Singleton` داشت، ولی `:data` هیچ‌وقت
    // وابستگیِ Dagger/Hilt نداره (فقط Room) - همون constructor injection باعثِ شکستِ
    // `:data:kaptDebugKotlin` با `error.NonExistentClass` رو annotationها می‌شد، چون `javax.inject`
    // درست رو classpathِ آنوتیشن‌پروسسینگِ اون ماژول نبود. مثلِ بقیه‌ی ریپازیتوری‌ها (که همه کلاسِ
    // سادن و اینجا provide می‌شن) برگشت به الگوی معمولی.
    @Provides
    fun provideInboxDao(database: AppDatabase): InboxMessageDao = database.inboxDao()

    @Provides
    @Singleton
    fun provideInboxRepository(dao: InboxMessageDao): InboxRepository = InboxRepository(dao)

    @Provides
    @Singleton
    fun provideGamificationRepository(
        coinDao: CoinDao,
        achievementDao: AchievementDao,
    ): GamificationRepository = GamificationRepository(coinDao, achievementDao)

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideBadgeEvaluator(
        gamification: GamificationRepository,
        accountRepository: AccountRepository,
        loanRepository: LoanRepository,
        savingsGoalRepository: SavingsGoalRepository,
        uiPrefs: UiPrefs,
    ): BadgeEvaluator =
        BadgeEvaluator(gamification, accountRepository, loanRepository, savingsGoalRepository, uiPrefs)

    @Provides
    @Singleton
    fun provideSavingsGoalDao(database: AppDatabase): SavingsGoalDao = database.savingsGoalDao()

    @Provides
    @Singleton
    fun provideSavingsGoalRepository(dao: SavingsGoalDao): SavingsGoalRepository =
        SavingsGoalRepository(dao)

    @Provides
    @Singleton
    fun provideParsingRuleDao(database: AppDatabase): ParsingRuleDao = database.parsingRuleDao()

    @Provides
    @Singleton
    fun provideParsingRuleRepository(dao: ParsingRuleDao): ParsingRuleRepository = ParsingRuleRepository(dao)

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository = CategoryRepository(categoryDao)
}
