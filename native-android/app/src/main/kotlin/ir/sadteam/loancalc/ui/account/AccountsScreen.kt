package ir.sadteam.loancalc.ui.account

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.data.accountIconForKey
import ir.sadteam.loancalc.data.db.ACCOUNT_TYPE_BANK
import ir.sadteam.loancalc.data.db.AccountEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppHeroCard
import ir.sadteam.loancalc.ui.components.BankBadge
import ir.sadteam.loancalc.ui.components.EmptyState
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.components.HeroMuted
import ir.sadteam.loancalc.ui.components.HeroPillBg
import ir.sadteam.loancalc.ui.components.InAppBannerHost
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.components.rememberInAppBanner
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFa
import ir.sadteam.loancalc.ui.jibak.toFaMoney
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppIconFrame
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppRadius
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.Motion

/**
 * پورت مفهومی ماژول «حساب» اپ رقیب (VAMMAN) - لیست حساب‌های بانکی با موجودی فعلی (محاسبه‌شده از رو
 * تراکنش‌ها، نه یه فیلد ثابت)، افزودن/ویرایش/حذف حساب، و جزئیات هر حساب (دفترچه‌ی تراکنش). الگوی
 * navigation داخلی عین ChequeScreen (screenKey مشتق‌شده + AnimatedContent).
 *
 * ⚠️ **کارتِ پشتیبان‌گیری از اینجا رفت** - زیرصفحه‌ی «داده‌ها و پشتیبان» تو تنظیمات پشتیبانِ کلِ
 * برنامه رو می‌گیره، دو دکمه‌ی هم‌نام با دامنه‌ی متفاوت گیج‌کننده بود، و تو حالتِ خالی این کارت
 * **بالای** پیامِ «هنوز حسابی ثبت نشده» می‌نشست.
 */
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    // خواسته‌ی صریحِ کاربر: افزودنِ حساب دیگه فقط از تنظیمات نباشه، از تبِ «دارایی» هم مستقیم قابلِ‌
    // دسترسی باشه - وقتی true باشه، این صفحه مستقیم با فرمِ بازِ افزودنِ حساب باز می‌شه، نه لیستِ خالی.
    startInAddMode: Boolean = false,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    var showAddForm by remember { mutableStateOf(startInAddMode) }
    var editingAccountId by remember { mutableStateOf<Long?>(null) }
    var openedAccountId by remember { mutableStateOf<Long?>(null) }

    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    // موجودی هر حساب فقط وقتی حساب‌ها/تراکنش‌ها عوض می‌شن دوباره حساب می‌شه، نه هر recomposition
    // به‌ازای هر کارت (قبلاً balanceOf رو تک‌تک آیتم‌ها هر بار صدا زده می‌شد).
    val balances = remember(accounts, transactions) {
        accounts.associate { it.id to viewModel.balanceOf(it, transactions) }
    }
    // جمعِ موجودی برای هیرویِ بالای لیست - همون balances که از قبل حساب شده، جمعش بی‌هزینه‌ست.
    val totalBalance = remember(balances) { balances.values.sum() }
    val openedAccount = openedAccountId?.let { id -> accounts.firstOrNull { it.id == id } }
    val editingAccount = editingAccountId?.let { id -> accounts.firstOrNull { it.id == id } }

    val banner = rememberInAppBanner()

    val screenKey = when {
        showAddForm -> "add"
        openedAccount != null -> "detail"
        else -> "list"
    }

    // ⚠️ AnimatedContent پشته‌ی خودش را دارد ولی بازگشتِ سیستمی از آن بی‌خبر بود: کاربر از
    // فرمِ افزودن دکمه‌ی back می‌زد و کلِ صفحه بسته می‌شد، با فرمِ نیمه‌پرشده. ترتیب همان
    // ترتیبِ screenKey است - بازترین لایه اول.
    BackHandler(enabled = screenKey != "list") {
        when {
            showAddForm -> {
                // `startInAddMode` یعنی این صفحه فقط پوسته‌ی فرم است، پس تا آخر برمی‌گردد -
                // همان قاعده‌ی onSaved/onCancel.
                if (startInAddMode && editingAccount == null) onBack()
                else { showAddForm = false; editingAccountId = null }
            }
            else -> openedAccountId = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = screenKey,
            transitionSpec = { Motion.contentEnter togetherWith Motion.contentExit },
            label = "accountsScreen",
        ) { key ->
            when (key) {
                "add" -> AddEditAccountScreen(
                    existing = editingAccount,
                    // `startInAddMode` یعنی این صفحه فقط پوسته‌ی فرمه، پس هر دو راهِ خروج تا آخر
                    // برمی‌گردن. شرطِ `editingAccount == null` لازمه وگرنه ویرایشِ حسابی که از
                    // فهرستِ همین صفحه باز شده هم به بیرون می‌پره.
                    onSaved = {
                        if (startInAddMode && editingAccount == null) onBack()
                        else { showAddForm = false; editingAccountId = null }
                    },
                    onCancel = {
                        if (startInAddMode && editingAccount == null) onBack()
                        else { showAddForm = false; editingAccountId = null }
                    },
                    viewModel = viewModel,
                )
                "detail" -> openedAccount?.let { account ->
                    AccountDetailScreen(
                        account = account,
                        onBack = { openedAccountId = null },
                        onEdit = { editingAccountId = account.id; openedAccountId = null; showAddForm = true },
                        onDelete = { viewModel.deleteAccount(account); openedAccountId = null },
                        viewModel = viewModel,
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp, 12.dp, 14.dp, 152.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                            }
                            Text("حساب‌های بانکی", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }

                    if (accounts.isNotEmpty()) {
                        item {
                            AccountsTotalHero(total = totalBalance, balances = balances, accounts = accounts)
                        }
                    }

                    if (accounts.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Outlined.AccountBalance,
                                title = "هنوز حسابی ثبت نشده",
                                description = "حساب‌های بانکیت رو اضافه کن تا موجودی و " +
                                    "گردشِ هرکدوم رو یک‌جا داشته باشی.",
                            )
                        }
                    } else {
                        items(accounts, key = { it.id }) { account ->
                            val balance = balances[account.id] ?: account.initialBalance
                            AccountCard(
                                account = account,
                                balance = balance,
                                share = if (totalBalance > 0) (balance / totalBalance).toFloat().coerceIn(0f, 1f) else 0f,
                                onClick = { openedAccountId = account.id },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }

        InAppBannerHost(banner, modifier = Modifier.align(Alignment.BottomCenter))

        // «+ افزودن حساب» چسبیده به پایینِ صفحه (خواسته‌ی طراحی: همیشه در دست باشه) - فقط تو حالتِ
        // لیست نشون داده می‌شه، نه موقعِ افزودن/جزئیات.
        if (screenKey == "list") {
            GradientButton(
                onClick = { showAddForm = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 24.dp),
            ) {
                Text("+ افزودن حساب")
            }
        }
    }
}

/** هیرویِ جمعِ موجودیِ بالای لیست - عددِ کل + نوارِ سهمِ هر حساب از کل (بدونِ کوئریِ جدید، از همون
 * balancesِ ازقبل‌محاسبه‌شده). */
@Composable
private fun AccountsTotalHero(total: Double, balances: Map<Long, Double>, accounts: List<AccountEntity>) {
    // کارتِ `26b`ی طرح: سبزِ توپر با متنِ سفید. عددِ قهرمان ۲۶/۹۰۰ و برچسبِ بالاش ۱۰٫۵/۷۰۰.
    AppHeroCard {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("جمعِ موجودی", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        // ⚠️ `total` **ریال** است و زیرش «تومان» نوشته می‌شد: عدد ده برابر
                        // بزرگ چاپ می‌شد. `rialToToman` پیش از فرمت.
                        rialToToman(total.toLong()).toFaMoney(),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                    Text("تومان · ${accounts.size.toFa()} حساب", color = HeroMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(AppRadius.icon))
                        .background(HeroPillBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            // نوارِ سهمِ هر حساب - رو زمینه‌ی سبز با سفیدهای کم‌آلفا کشیده می‌شه، نه سبزهای کم‌آلفا
            // (که رو خودِ سبز اصلاً دیده نمی‌شدن).
            Row(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(AppRadius.button)),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // ⚠️ فهرستِ سه‌تایی بود و حسابِ چهارم به بعد همه ۰٫۳۰ می‌گرفتند - سه تکه‌ی
                // یک‌رنگِ چسبیده که از هم تفکیک نمی‌شدند. حالا از ۰٫۹۲ تا ۰٫۲۲ پخش می‌شود،
                // هر چند حساب که باشد.
                val step = if (accounts.size > 1) 0.70f / (accounts.size - 1) else 0f
                accounts.forEachIndexed { index, account ->
                    val balance = balances[account.id] ?: account.initialBalance
                    // حسابِ منفی وزنِ منفی می‌داد؛ coerceAtLeast تکه را نگه می‌داشت ولی جمعِ
                    // وزن‌ها را به‌هم می‌ریخت. قدرِ مطلق درست‌تر است: سهمِ **حجمی**.
                    val weight = if (total > 0) {
                        (kotlin.math.abs(balance) / total).toFloat().coerceIn(0.02f, 1f)
                    } else {
                        1f / accounts.size
                    }
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.92f - step * index)),
                    )
                }
            }
        }
    }
}

/** بجِ ردیف: `BankBadge` برای بانکی، `accountIconForKey` برای غیربانکی - هم‌الگو با `AccountRow`ِ
 * تبِ دارایی. جوهرِ متن `AppPrimaryInk`/`AppDangerInk` (روی سطح)، نه `AppPrimary`/`AppDanger`. */
@Composable
private fun AccountCard(
    account: AccountEntity,
    balance: Double,
    share: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.pressScaleClickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (account.type == ACCOUNT_TYPE_BANK) {
                BankBadge(bankName = account.bankName, size = 38.dp)
            } else {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(AppIconFrame),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        accountIconForKey(account.iconKey),
                        contentDescription = null,
                        tint = AppMuted,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(account.name, color = AppText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        // ریال → تومان، مثلِ هیرو. فرمِ کامل نه فشرده: این ستون عرض دارد و
                        // فهرستِ حساب جای عددِ دقیق است.
                        "${rialToToman(balance.toLong()).toFaMoney()} تومان",
                        // موجودیِ منفیِ کارتِ اعتباری وضعِ عادی است نه خطا: فقط عدد قرمز.
                        color = if (balance < 0) AppDangerInk else AppText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(account.bankName.ifBlank { "منبعِ نقدی" }, color = AppMuted, fontSize = 11.sp)
                    // «٪» **بعد** از عدد می‌آید نه قبلش (قاعده‌ی toFaPercent). و حسابِ منفی
                    // سهم ندارد: «٪−۱۲ از دارایی» بی‌معنا بود.
                    Text(
                        if (share <= 0f) "بی‌سهم از دارایی"
                        else "${(share * 100).toInt().toFa()}٪ از دارایی",
                        color = AppMuted,
                        fontSize = 10.sp,
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(999.dp)).background(AppPrimaryPill),
                ) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(share.coerceIn(0f, 1f)).background(AppPrimaryInk))
                }
            }
        }
    }
}
