package ir.sadteam.loancalc.ui.account

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.core.MerchantCategoryGuesser
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.AccountRepository
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppChip
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppText
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class StatementImportViewModel @Inject constructor(private val repo: AccountRepository) : ViewModel() {
    val accounts = repo.observeAccounts()

    /** ثبت؛ ردیفی که همان حساب/روز/مبلغ/جهت را از قبل دارد تکراری حساب و رد می‌شود. برمی‌گرداند: (ثبت‌شده، تکراری). */
    suspend fun import(rows: List<StatementRow>, accountId: Long): Pair<Int, Int> {
        val existing = repo.observeTransactions().first().filter { it.accountId == accountId }
            .map { "${it.year}-${it.month}-${it.day}-${it.amount.toLong()}-${it.type}" }.toMutableSet()
        var added = 0
        var dup = 0
        val base = System.currentTimeMillis() * 10
        rows.forEachIndexed { i, r ->
            val type = if (r.deposit) TransactionType.DEPOSIT else TransactionType.WITHDRAWAL
            val key = "${r.date.y}-${r.date.m}-${r.date.d}-${r.amount.toLong()}-${type.name}"
            if (!existing.add(key)) { dup++; return@forEachIndexed }
            repo.addTransaction(
                accountId = accountId,
                type = type,
                amount = r.amount,
                description = r.description,
                year = r.date.y, month = r.date.m, day = r.date.d,
                category = MerchantCategoryGuesser.guess(r.description, !r.deposit) ?: if (r.deposit) "سایر درآمد" else "سایر هزینه",
                id = base + i, // شمارنده‌ی صریح - قاعده‌ی حلقه در AccountRepository
                originLabel = "صورت‌حساب",
            )
            added++
        }
        ir.sadteam.loancalc.data.UsageStats.action("statement_import")
        return added to dup
    }
}

/** 📄 **واردکردنِ صورت‌حسابِ بانکی** از فایلِ CSV یا Excel (۸ مهر، خواسته‌ی کاربر). */
@Composable
fun StatementImportScreen(onBack: () -> Unit, vm: StatementImportViewModel = hiltViewModel()) {
    // بازبینیِ ۹ مهر: ورودِ انبوه سقفِ ۳۰ تراکنشِ نسخه‌ی رایگان را دور می‌زد.
    ir.sadteam.loancalc.ui.subscription.PremiumBlock(blocked = true, key = "statement", label = "ورودِ صورت‌حساب", onBlocked = onBack)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val accounts by vm.accounts.collectAsState(initial = emptyList())
    var rows by remember { mutableStateOf<List<StatementRow>?>(null) }
    var fileName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var account by remember { mutableStateOf<AccountEntity?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            error = null; result = null
            val name = ctx.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && i >= 0) c.getString(i) else null
            } ?: "file.csv"
            val parsed = withContext(Dispatchers.IO) {
                runCatching { ctx.contentResolver.openInputStream(uri)!!.use { StatementParser.parse(name, it) } }.getOrNull()
            }
            fileName = name
            rows = parsed
            if (parsed.isNullOrEmpty()) error = "در این فایل ردیفی پیدا نشد. فایل باید ستونِ «تاریخ» و «مبلغ» (یا برداشت/واریز) داشته باشد. فایلِ .xls قدیمی را در Excel به .xlsx یا CSV ذخیره کن."
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت", tint = AppText) }
                Text("صورت‌حسابِ بانکی", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
        item {
            AppCard {
                Text("فایلِ صورت‌حساب را از اینترنت‌بانک بگیر", color = AppText, fontSize = 14.5.sp, fontWeight = FontWeight.Black)
                Text("CSV یا Excel (xlsx). تاریخ، شرح و مبلغ خودکار خوانده می‌شوند و ردیف‌های تکراری ثبت نمی‌شوند.", color = AppMuted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                GradientButton(onClick = { picker.launch(arrayOf("text/*", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (rows == null) "انتخابِ فایل" else "فایلِ دیگر")
                }
                error?.let { Text(it, color = AppDangerInk, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
        val list = rows.orEmpty()
        if (list.isNotEmpty()) {
            item {
                AppCard {
                    Text(fileName, color = AppMuted, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${toFa(list.size)} تراکنش", color = AppText, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(
                        "واریز ${list.filter { it.deposit }.sumOf { it.amount }.rialToFaCompact()} · برداشت ${list.filter { !it.deposit }.sumOf { it.amount }.rialToFaCompact()} تومان",
                        color = AppMuted, fontSize = 12.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("به کدام حساب؟", color = AppText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    if (accounts.isEmpty()) {
                        Text("اول یک حساب بساز.", color = AppDangerInk, fontSize = 12.sp)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            items(accounts, key = { it.id }) { a -> AppChip(label = a.name, selected = account?.id == a.id, onClick = { account = a }) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    GradientButton(
                        enabled = account != null && !busy,
                        onClick = {
                            val acc = account ?: return@GradientButton
                            busy = true
                            scope.launch {
                                val (added, dup) = vm.import(list, acc.id)
                                result = "${toFa(added)} تراکنش ثبت شد" + if (dup > 0) " · ${toFa(dup)} تکراری رد شد" else ""
                                rows = null
                                busy = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("ثبتِ ${toFa(list.size)} تراکنش") }
                }
            }
            items(list.take(30)) { r ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${toFa(r.date.d)}/${toFa(r.date.m)}", color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.width(44.dp))
                    Text(r.description, color = AppText, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(
                        (if (r.deposit) "+" else "−") + r.amount.rialToFaCompact(),
                        color = if (r.deposit) AppPrimary else AppDangerInk, fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (list.size > 30) item { Text("و ${toFa(list.size - 30)} ردیفِ دیگر", color = AppMuted, fontSize = 11.5.sp, modifier = Modifier.padding(6.dp)) }
        }
        result?.let { item { AppCard { Text(it, color = AppPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black) } } }
    }
}
