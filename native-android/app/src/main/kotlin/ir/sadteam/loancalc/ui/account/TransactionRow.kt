package ir.sadteam.loancalc.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.sadteam.loancalc.core.TransactionType
import ir.sadteam.loancalc.data.db.AccountTransactionEntity
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.jibak.rialToToman
import ir.sadteam.loancalc.ui.jibak.toFaSignedMoney
import ir.sadteam.loancalc.ui.jibak.toFaTime
import ir.sadteam.loancalc.ui.privacy.LocalPrivacyMode
import ir.sadteam.loancalc.ui.privacy.PrivacyCrossfade
import ir.sadteam.loancalc.ui.privacy.maskIfPrivate
import ir.sadteam.loancalc.ui.theme.AppChipBg
import ir.sadteam.loancalc.ui.theme.AppDangerPill
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppPrimaryPill
import ir.sadteam.loancalc.ui.theme.AppText
import ir.sadteam.loancalc.ui.theme.AppTxIn
import ir.sadteam.loancalc.ui.theme.AppTxOut

/**
 * ردیفِ تراکنش - فریمِ `71a`، قاعده‌های `71c`.
 *
 * **سه چیز، سه جا:** مبلغِ باعلامت سمتِ چپ · منبع و ساعت خطِ دومِ ریز · نوع، آیکونِ سمتِ
 * راست. هیچ‌کدام تکرار نمی‌شود، پس ردیف از ~۹۰px به ~۵۰ می‌رسد (چهار تراکنش در صفحه به نُه).
 *
 * **سه آیکون و نه بیشتر** (قاعده‌ی ۲): اعلان · پیامک · دستی. خانه‌ی آیکون **هم‌رنگِ جهت**
 * است برای خودکارها و خنثی برای دستی، پس منبع و جهت با یک نگاه خوانده می‌شوند. لوگوی بانک
 * عمداً نمی‌آید - سی‌وچهار لوگو در ۳۰px لکه می‌شوند و نامِ بانک از قبل در متن است.
 *
 * تاریخ این‌جا نیست: سرگروهِ روز بالای گروه می‌نشیند (قاعده‌ی ۴) و ساعت جایش را می‌گیرد.
 */
@Composable
fun CompactTransactionRow(
    tx: AccountTransactionEntity,
    modifier: Modifier = Modifier,
    /** `71d`: در شیتِ «خرجِ امروز» نامِ حساب پیشِ منبع می‌آید - چند حساب با هم قاطی‌اند. */
    accountName: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val isIn = tx.type == TransactionType.DEPOSIT.name
    val ink = if (isIn) AppTxIn else AppTxOut
    val privacyMode = LocalPrivacyMode.current
    // «دستی» صریح نوشته می‌شود، نه با خالی‌گذاشتنِ منبع (قاعده‌ی ۳): خطِ خالی یعنی «نمی‌دانم».
    val origin = tx.originLabel?.takeIf { it.isNotBlank() } ?: "دستی"
    val icon: ImageVector = when {
        tx.originLabel == null -> Icons.Filled.Add
        tx.originLabel.startsWith("پیامک") -> Icons.Filled.ChatBubble
        else -> Icons.Filled.Notifications
    }
    // خانه‌ی آیکونِ ثبتِ دستی خنثی است - رنگِ جهت را فقط خودکارها می‌گیرند.
    val iconBg = when {
        tx.originLabel == null -> AppChipBg
        isIn -> AppPrimaryPill
        else -> AppDangerPill
    }
    val iconTint = if (tx.originLabel == null) AppMuted else ink

    AppCard(modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(15.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tx.description.ifBlank { if (isIn) "واریز" else "برداشت" },
                    color = AppText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(accountName, origin, timeOfTransaction(tx)).joinToString(" · "),
                    color = AppMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            PrivacyCrossfade(privacyMode) { masked ->
                Text(
                    maskIfPrivate(
                        masked,
                        rialToToman(tx.amount.toLong()).let { if (isIn) it else -it }.toFaSignedMoney(),
                    ),
                    color = ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

/**
 * ساعتِ ثبت از `createdAt` (ISO). کاربر خواست بداند تراکنش **کِی** آمده، نه فقط کدام روز.
 *
 * ردیف‌های قدیمی که `createdAt`شان قابلِ خواندن نیست ساعت نمی‌گیرند - `null` برمی‌گردد و
 * `joinToString` خودش حذفش می‌کند (نه «۰۰:۰۰»ِ دروغ).
 */
fun timeOfTransaction(tx: AccountTransactionEntity): String? {
    val t = tx.createdAt.substringAfter('T', "").take(5)
    if (t.length != 5 || t[2] != ':') return null
    val h = t.take(2).toIntOrNull() ?: return null
    val m = t.drop(3).toIntOrNull() ?: return null
    return toFaTime(h, m)
}
