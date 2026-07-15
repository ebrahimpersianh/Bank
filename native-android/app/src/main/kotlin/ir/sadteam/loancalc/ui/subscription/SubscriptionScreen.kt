package ir.sadteam.loancalc.ui.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.sadteam.loancalc.subscription.LocalSubscriptionManager
import ir.sadteam.loancalc.subscription.subscriptionTiers
import ir.sadteam.loancalc.ui.auth.AuthViewModel
import ir.sadteam.loancalc.ui.components.AppCard
import ir.sadteam.loancalc.ui.components.GradientButton
import ir.sadteam.loancalc.ui.theme.AppDanger
import ir.sadteam.loancalc.ui.theme.AppMuted
import ir.sadteam.loancalc.ui.theme.AppText

/**
 * پورت #subscriptionModal تو www/index.html - ۴ پلن پلکانی، خرید واقعی با SDK بومی Poolakey
 * (نه پلاگین Capacitor)، تایید سمت سرور قبل از فعال‌شدن. اگه کافه‌بازار رو گوشی نصب نباشه یا
 * سرویس وصل نشه، [LocalSubscriptionManager] پیام صادقانه‌ی «فقط رو نسخه‌ی نصبی کار می‌کنه» می‌ده.
 */
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    onSubscribed: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val subscriptionManager = LocalSubscriptionManager.current
    var prices by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var purchasingProductId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(subscriptionManager) {
        subscriptionManager?.getPrices(
            productIds = subscriptionTiers.map { it.first },
            onResult = { prices = it },
            onError = { },
        )
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                }
                Text("اشتراک", color = AppText, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        if (subscriptionManager == null) {
            item {
                Text(
                    "این قابلیت فقط رو نسخه‌ی نصبی اپ (از کافه‌بازار) کار می‌کنه.",
                    color = AppMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        } else {
            items(subscriptionTiers) { (productId, label) ->
                AppCard(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(label, color = AppText, fontSize = 14.sp)
                            Text(prices[productId] ?: "…", color = AppMuted, fontSize = 12.sp)
                        }
                        GradientButton(
                            enabled = purchasingProductId == null,
                            onClick = {
                                error = null
                                purchasingProductId = productId
                                subscriptionManager.purchase(
                                    productId = productId,
                                    onSucceed = { purchaseToken ->
                                        authViewModel.verifySubscriptionPurchase(
                                            productId = productId,
                                            purchaseToken = purchaseToken,
                                            onSuccess = { purchasingProductId = null; onSubscribed() },
                                            onError = {
                                                purchasingProductId = null
                                                error = "تایید خرید ناموفق بود؛ اگه پول کم شده با پشتیبانی تماس بگیر"
                                            },
                                        )
                                    },
                                    onFailed = { purchasingProductId = null; error = "خرید ناموفق بود" },
                                    onCanceled = { purchasingProductId = null },
                                )
                            },
                        ) {
                            Text(if (purchasingProductId == productId) "..." else "خرید")
                        }
                    }
                }
            }
        }

        if (error != null) {
            item {
                Text(
                    text = error ?: "",
                    color = AppDanger,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}
