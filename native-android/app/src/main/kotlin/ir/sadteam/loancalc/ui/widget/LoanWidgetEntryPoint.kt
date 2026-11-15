package ir.sadteam.loancalc.ui.widget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.sadteam.loancalc.data.LoanRepository

/**
 * ویجت (GlanceAppWidget) بخشی از درختِ @AndroidEntryPoint نیست (نه Activity/Fragment/Service)،
 * پس نمی‌تونه مستقیم `hiltViewModel()`/تزریقِ سازنده استفاده کنه؛ این EntryPoint راهِ رسمیِ Hilt
 * برای گرفتنِ یه Repository از بیرونِ اون درخته.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface LoanWidgetEntryPoint {
    fun loanRepository(): LoanRepository
}
