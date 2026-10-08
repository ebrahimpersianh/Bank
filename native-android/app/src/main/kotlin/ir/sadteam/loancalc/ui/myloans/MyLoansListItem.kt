@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ir.sadteam.loancalc.ui.myloans

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import ir.sadteam.loancalc.core.PersianDate
import ir.sadteam.loancalc.core.toFa
import ir.sadteam.loancalc.data.db.LoanEntity
import ir.sadteam.loancalc.ui.auth.GateState
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.AppCardVariant
import ir.sadteam.loancalc.ui.components.PaidRing
import ir.sadteam.loancalc.ui.components.SettledMedal
import ir.sadteam.loancalc.ui.components.pressScaleClickable
import ir.sadteam.loancalc.ui.jibak.rialToFaCompact
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppDangerInk
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppLabel
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimary
import ir.sadteam.loancalc.ui.theme.AppPrimaryInk
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import kotlin.math.roundToInt
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.lazy.LazyItemScope

@Composable
internal fun LazyItemScope.MyLoanListItem(
    loan: LoanEntity,
    viewModel: MyLoansViewModel,
    loanCardHeights: androidx.compose.runtime.snapshots.SnapshotStateMap<Long, Int>,
    buzz: () -> Unit,
    gateState: GateState?,
    isLoanLocked: (LoanEntity) -> Boolean,
    openedLoanIdState: MutableState<Long?>,
    showLoginPromptState: MutableState<Boolean>,
    showSubscriptionScreenState: MutableState<Boolean>,
    sortOptionState: MutableState<LoanSortOption>,
    settledDatesState: MutableState<Map<Long, PersianDate>>,
    draggingLoanIdState: MutableState<Long?>,
    dragOffsetYState: MutableState<Float>,
    orderedLoansState: MutableState<List<LoanEntity>>,
    heroOriginState: MutableState<androidx.compose.ui.graphics.TransformOrigin>,
    listBoundsState: MutableState<androidx.compose.ui.geometry.Rect>,
) {
    var openedLoanId by openedLoanIdState
    var showLoginPrompt by showLoginPromptState
    var showSubscriptionScreen by showSubscriptionScreenState
    var sortOption by sortOptionState
    var settledDates by settledDatesState
    var draggingLoanId by draggingLoanIdState
    var dragOffsetY by dragOffsetYState
    var orderedLoans by orderedLoansState
    var heroOrigin by heroOriginState
    var listBounds by listBoundsState
                            // انیمیشنِ فلیپِ کارت (خواسته‌ی «انیمیشن‌های سفارشی») - آیکونِ اطلاعات، کارت رو
                            // مثل یه چکِ فیزیکی می‌چرخونه و خلاصه‌ی پرداخت رو پشتش نشون می‌ده؛ ضربه‌ی اصلیِ
                            // کارت هنوز باز کردنِ جزئیاتِ وامه، این فقط یه لایه‌ی جدا و مستقله.
                            var flipped by remember { mutableStateOf(false) }
                            val density = LocalDensity.current
                            val rotation by animateFloatAsState(
                                targetValue = if (flipped) 180f else 0f,
                                animationSpec = tween(500),
                                label = "loanCardFlip",
                            )
                            // animateItem: اضافه/حذف/جابه‌جایی وام‌ها با انیمیشن نرم (نه پرش یهویی).
                            var cardBounds by remember { mutableStateOf(Rect.Zero) }
                            val isDragging = loan.id == draggingLoanId
                            // بازپرداختِ عقب‌افتاده: سررسیدِ اولین قسطِ پرداخت‌نشده از امروز گذشته -
                            // خودِ کارت حاشیه‌ی قرمز می‌گیره + یه بجِ «!» کنارِ اسمِ وام.
                            val overdue = remember(loan) { viewModel.isLoanOverdue(loan) }
                            // فریمِ 27a سه حالتِ ردیف داره و کارتِ **فوری** مالِ «سررسیدِ نزدیک»ه
                            // (تو خودِ فریم: وامی که فردا قسط داره)، نه فقط عقب‌افتاده. آستانه‌ی
                            // «نزدیک» سه روزه، همون آستانه‌ی یادآورِ اپ.
                            val nextDue = remember(loan) { viewModel.getLoanNextDueDate(loan) }
                            val daysToDue = remember(nextDue) { nextDue?.let { viewModel.daysUntilToday(it) } }
                            val dueSoon = daysToDue != null && daysToDue in 0..3
                            val isLocked = isLoanLocked(loan)
                            val attention = (overdue || dueSoon) && !isLocked
                            // وامِ عقب‌افتاده گونه‌ی **فوریِ** کارت رو می‌گیره (زمینه‌ی صورتیِ کم‌رنگ
                            // + حاشیه و سایه‌ی قرمز)، نه فقط یه حاشیه‌ی قرمز رو کارتِ سفید -
                            // طبقِ گونه‌ی «فوری»ِ بخشِ ۵ سیستمِ طراحی.
                            AppCard(
                                variant = when {
                                    isLoanSettled(loan) -> AppCardVariant.DONE
                                    else -> AppCardVariant.DEFAULT
                                },
                                // مدالِ تسویه نباید با بقیه‌ی محتوا محو بشه.
                                dimContent = false,
                                modifier = Modifier
                                    .zIndex(if (isDragging) 1f else 0f)
                                    .then(if (isDragging) Modifier else Modifier.animateItem())
                                    .onGloballyPositioned {
                                        cardBounds = it.boundsInRoot()
                                        loanCardHeights[loan.id] = it.size.height
                                    }
                                    // نگه‌داشتنِ چندثانیه‌ای رو کارت، بعد کشیدن بالا/پایین برای
                                    // جابه‌جاییِ دستیِ ترتیبِ لیست - خواسته‌ی صریحِ کاربر. تپِ سریعِ
                                    // معمولی (بدونِ نگه‌داشتن) دستِ detectDragGesturesAfterLongPress
                                    // رو نمی‌رسه، همون pressScaleClickable پایین‌تر جواب می‌ده.
                                    .pointerInput(loan.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingLoanId = loan.id
                                                dragOffsetY = 0f
                                                buzz()
                                                if (sortOption != LoanSortOption.CUSTOM) sortOption = LoanSortOption.CUSTOM
                                            },
                                            onDragEnd = {
                                                draggingLoanId = null
                                                dragOffsetY = 0f
                                                viewModel.reorderLoans(orderedLoans)
                                            },
                                            onDragCancel = {
                                                draggingLoanId = null
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val currentIndex = orderedLoans.indexOfFirst { it.id == loan.id }
                                                val step = (loanCardHeights[loan.id] ?: 200) + 10
                                                if (dragOffsetY > step / 2 && currentIndex < orderedLoans.lastIndex) {
                                                    orderedLoans = orderedLoans.toMutableList().apply {
                                                        add(currentIndex + 1, removeAt(currentIndex))
                                                    }
                                                    dragOffsetY -= step
                                                } else if (dragOffsetY < -step / 2 && currentIndex > 0) {
                                                    orderedLoans = orderedLoans.toMutableList().apply {
                                                        add(currentIndex - 1, removeAt(currentIndex))
                                                    }
                                                    dragOffsetY += step
                                                }
                                            },
                                        )
                                    }
                                    .pressScaleClickable {
                                        if (isLocked) {
                                            // نه واردِ جزئیات می‌شه نه چیزی پاک/عوض می‌کنه - فقط
                                            // مستقیم می‌بره سراغِ خریدِ اشتراک، چون تنها راهِ بازشدنِ
                                            // این وام همونه.
                                            if (gateState != GateState.LOGGED_IN) {
                                                showLoginPrompt = true
                                            } else {
                                                showSubscriptionScreen = true
                                            }
                                            return@pressScaleClickable
                                        }
                                        // مرکزِ همین کارت رو به کسرِ ۰..۱ از کلِ صفحه تبدیل می‌کنیم تا
                                        // بزرگ‌شدنِ صفحه‌ی جزئیات دقیقاً از همین‌جا شروع بشه.
                                        heroOrigin = cardBounds.heroOriginIn(listBounds)
                                        openedLoanId = loan.id
                                    }
                                    .graphicsLayer {
                                        rotationY = rotation
                                        cameraDistance = 12f * density.density
                                        translationY = if (isDragging) dragOffsetY else 0f
                                        alpha = if (isLocked) 0.55f else 1f
                                    },
                            ) {
                                if (rotation <= 90f) {
                                    // ردیفِ وام طبقِ فریمِ `27a` - سه حالت: سررسیدِ نزدیک (کارتِ
                                    // فوری + دکمه‌ی پرداخت)، در جریان (کارتِ معمولی + شِورون)،
                                    // تسویه‌شده (کارتِ تمام‌شده + مدال). حلقه همیشه سمتِ راست.
                                    val settled = isLoanSettled(loan)
                                    val paidPct = if (loan.n > 0) loan.paidCount.toFloat() / loan.n else 0f
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // 🚨 **کاشیِ نشان جای حلقه، سمتِ راست** (طرحِ مرجعِ
                                        // کاربر: «باکسِ هر وام مثلِ همین عکس با همان آیکون‌ها»).
                                        // حلقه رفت سمتِ چپ و ریزتر شد؛ چیزی که چشم اول باید
                                        // بگیرد **نوعِ وام** است نه درصدش، و ده ردیفِ حلقه‌دارِ
                                        // هم‌شکل دقیقاً همان بی‌روحی‌ای بود که کاربر گفت.
                                        Icon(
                                            Icons.Filled.ChevronLeft,
                                            contentDescription = null,
                                            tint = AppLabel,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .then(if (settled) Modifier.alpha(0.7f) else Modifier),
                                            verticalArrangement = Arrangement.spacedBy(3.dp),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    loan.name,
                                                    color = AppText,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false),
                                                )
                                                // بجِ وضعیت کنارِ اسم (طرحِ مرجع) - رنگِ کارت
                                                // همین را می‌گفت ولی بی کلمه، و کسی که رنگ را
                                                // نمی‌خوانَد هیچ‌وقت نمی‌فهمید کدام عقب‌افتاده است.
                                                val stateLabel = when {
                                                    settled -> "تسویه شده"
                                                    // ۱۴ مهر: «معوق» حذف - خطِ قرمزِ «عقب‌افتاده» زیرش همین را می‌گوید.
                                                    overdue -> null
                                                    dueSoon -> "نزدیک"
                                                    else -> "در جریان"
                                                }
                                                if (stateLabel != null) {
                                                    Text(
                                                        stateLabel,
                                                        color = if (overdue || dueSoon) AppDangerInk else AppPrimaryInk,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier
                                                            .padding(start = 8.dp)
                                                            .clip(RoundedCornerShape(999.dp))
                                                            .background(if (overdue || dueSoon) AppDangerPill else AppPrimaryPill)
                                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                                    )
                                                }
                                                if (isLocked) {
                                                    Icon(
                                                        Icons.Filled.Lock,
                                                        contentDescription = "این وام قفله - برای بازکردنش مشترک شو",
                                                        tint = AppMuted,
                                                        modifier = Modifier.padding(start = 6.dp).size(13.dp),
                                                    )
                                                }
                                            }
                                            // سطرِ دوم طبقِ فریم: **تاریخِ سررسید** + مبلغِ قسط
                                            // («۲۸ شهریور · ۹۵۰٬۰۰۰»)، و برای تسویه‌شده تاریخِ
                                            // تسویه («تسویه شد · تیر ۱۴۰۵»). کلمه‌ی «در جریان»
                                            // اطلاعِ صفر داشت - همه‌ی ردیف‌های لیستِ فعال در جریان‌اند.
                                            PrivacyCrossfade(LocalPrivacyMode.current) { masked ->
                                                Text(
                                                    buildString {
                                                        if (settled) {
                                                            append("تسویه شد · ")
                                                            append(
                                                                settledDates[loan.id]
                                                                    ?.let { jalaliMonthYearOf(it) } ?: "—",
                                                            )
                                                        } else {
                                                            append(
                                                                when {
                                                                    overdue -> "عقب‌افتاده"
                                                                    dueSoon -> dueSoonLabel(daysToDue!!)
                                                                    else -> nextDue?.let { jalaliShortOf(it) } ?: "—"
                                                                },
                                                            )
                                                            append(" · ")
                                                            append(maskIfPrivate(masked, loan.installment.rialToFaCompact()))
                                                            append(" تومان")
                                                        }
                                                    },
                                                    color = when {
                                                        settled -> AppPrimaryInk
                                                        overdue -> AppDangerInk
                                                        dueSoon -> AppDangerInk
                                                        else -> AppMuted
                                                    },
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                            // سطرِ سوم فقط برای وامِ بازه - وامِ تسویه‌شده نداردش.
                                            if (!settled) {
                                                // دو نشانِ ریز جای نقطه‌ی جداکننده (طرحِ مرجع):
                                                // چشم «۲ از ۱۲» و نامِ بانک را جدا می‌بیند، نه
                                                // یک رشته‌ی طولانیِ یکدست.
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(top = 2.dp),
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.EventNote,
                                                        contentDescription = null,
                                                        tint = AppLabel,
                                                        modifier = Modifier.size(13.dp),
                                                    )
                                                    Text(
                                                        "${toFa(loan.paidCount)} از ${toFa(loan.n)} قسط",
                                                        color = AppMuted,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                    Icon(
                                                        Icons.Outlined.AccountBalance,
                                                        contentDescription = null,
                                                        tint = AppLabel,
                                                        modifier = Modifier.padding(start = 6.dp).size(13.dp),
                                                    )
                                                    Text(
                                                        loan.bank,
                                                        color = AppMuted,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                        // ستونِ چپ: درصد بالا، کنشِ ردیف پایین - همان چیدمانِ
                                        // طرحِ مرجع. حلقه این‌جا **ریز** است چون خبرِ درجه‌دوم
                                        // است؛ خبرِ اول مبلغِ عقب‌افتاده‌ی وسطِ ردیف است.
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            // فاصله‌ی حلقه تا دکمه - قبلاً دکمه به تهِ حلقه چسبیده بود (عکسِ کاربر).
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            if (settled) {
                                                SettledMedal(diskSize = 34.dp)
                                            } else {
                                                PaidRing(
                                                    fraction = paidPct,
                                                    ringColor = if (attention) AppDangerInk else AppPrimary,
                                                    trackColor = (if (attention) AppDangerInk else AppPrimary)
                                                        .copy(alpha = 0.2f),
                                                    centerTop = "${toFa((paidPct * 100).roundToInt())}٪",
                                                    centerBottom = "",
                                                    centerTopColor = if (attention) AppDangerInk else AppPrimary,
                                                    centerBottomColor = AppMuted,
                                                    size = 48.dp,
                                                    stroke = 4.5.dp,
                                                    centerTopSize = 12,
                                                )
                                            }
                                            if (!settled && !isLocked) {
                                                LoanPayButton(
                                                    onClick = {
                                                        heroOrigin = cardBounds.heroOriginIn(listBounds)
                                                        openedLoanId = loan.id
                                                    },
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer { rotationY = 180f },
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(loan.name, color = AppText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { flipped = false }) {
                                                Icon(Icons.Filled.Info, contentDescription = "بستن خلاصه", tint = AppMuted)
                                            }
                                        }
                                        PrivacyCrossfade(LocalPrivacyMode.current) { masked ->
                                            Text(
                                                "باقی‌مانده: ${maskIfPrivate(masked, amountToman(loan.installment * (loan.n - loan.paidCount)))} تومان",
                                                color = AppPrimary,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(top = 6.dp),
                                            )
                                        }
                                        Text(
                                            "${toFa(loan.n - loan.paidCount)} قسط باقیمانده از ${toFa(loan.n)}",
                                            color = AppMuted,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                            }
}
