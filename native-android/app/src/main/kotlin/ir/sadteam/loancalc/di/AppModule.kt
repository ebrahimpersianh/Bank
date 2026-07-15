package ir.sadteam.loancalc.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.sadteam.loancalc.data.AuthRepository
import ir.sadteam.loancalc.data.LoanRepository
import ir.sadteam.loancalc.data.db.AppDatabase
import ir.sadteam.loancalc.data.db.LoanDao
import ir.sadteam.loancalc.data.network.ApiClient
import ir.sadteam.loancalc.data.network.ApiService
import ir.sadteam.loancalc.data.prefs.AuthPrefs
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
    @Singleton
    fun provideLoanRepository(loanDao: LoanDao, apiService: ApiService): LoanRepository =
        LoanRepository(loanDao, apiService)

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
}
