package ir.sadteam.loancalc.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.ParsingRuleRepository
import ir.sadteam.loancalc.ui.inbox.QuickCategoryDialog
import ir.sadteam.loancalc.ui.theme.LoanCalcTheme
import ir.sadteam.loancalc.ui.theme.ThemeViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * 🧠 (۱۶ مهر) انتخابِ دسته **همان‌جا روی اعلان**: پنجره‌ای شفاف که بی بازکردنِ برنامه روی هر
 * صفحه‌ای باز می‌شود. دکمه‌های سریعِ اعلان هم از همین می‌گذرند (بی رابط، فوری) تا اعلان حتماً
 * بسته شود - گزارشِ کاربر: با زدنِ دکمه، اعلان بالا می‌ماند.
 */
@AndroidEntryPoint
class CategoryPickActivity : ComponentActivity() {
    @Inject lateinit var accounts: AccountRepository
    @Inject lateinit var rules: ParsingRuleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val txId = intent.getLongExtra(EXTRA_TX, -1L)
        NotificationManagerCompat.from(this).cancel(intent.getIntExtra(EXTRA_NOTIF, 0))
        if (txId <= 0) { finish(); return }
        val quick = intent.getStringExtra(EXTRA_CATEGORY)
        if (!quick.isNullOrBlank()) {
            lifecycleScope.launch {
                runCatching { CategoryLearning.apply(accounts, rules, txId, quick) }
                Toast.makeText(applicationContext, "دسته: $quick ✓", Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            val colorTheme by themeViewModel.colorTheme.collectAsState()
            LoanCalcTheme(themeMode = themeMode, colorTheme = colorTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    QuickCategoryDialog(
                        txId = txId,
                        onDismiss = { finish() },
                        onPicked = { Toast.makeText(applicationContext, "دسته: $it ✓", Toast.LENGTH_SHORT).show() },
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TX = "tx_id"
        private const val EXTRA_NOTIF = "notif_id"
        private const val EXTRA_CATEGORY = "category"

        fun intent(context: Context, txId: Long, notificationId: Int, category: String? = null) =
            Intent(context, CategoryPickActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                putExtra(EXTRA_TX, txId)
                putExtra(EXTRA_NOTIF, notificationId)
                category?.let { putExtra(EXTRA_CATEGORY, it) }
            }
    }
}
