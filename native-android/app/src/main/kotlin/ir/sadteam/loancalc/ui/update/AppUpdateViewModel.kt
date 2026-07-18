package ir.sadteam.loancalc.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.BuildConfig
import ir.sadteam.loancalc.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * آپدیتِ خودکار (نه کافه‌بازار نه مایکت API خودکار براش دارن) - رجوع کن به
 * server/routes/AppVersionRoutes.kt. هر بار اپ باز می‌شه یه‌بار سرور رو چک می‌کنه؛ اگه نسخه‌ی
 * سرور از BuildConfig.VERSION_CODE خودِ نصب بزرگ‌تر بود، لینکِ متناظرِ همون فلیور (cafebazaar/myket -
 * BuildConfig.FLAVOR از فلیورِ Gradle میاد، دقیقاً همون اسمی که تو app/build.gradle.kts تعریف شده)
 * رو برمی‌گردونه تا [updateUrl] یه بنر نشون بده. خطای شبکه/سرور بی‌صدا نادیده گرفته می‌شه - این فقط
 * یه یادآوریِ اختیاریه، نباید چیزی رو بلاک کنه.
 */
@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {
    private val _updateUrl = MutableStateFlow<String?>(null)
    val updateUrl: StateFlow<String?> = _updateUrl.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val result = apiService.getAppVersion()
                if (result.latestVersionCode > BuildConfig.VERSION_CODE) {
                    _updateUrl.value = when (BuildConfig.FLAVOR) {
                        "myket" -> result.myketUrl
                        else -> result.cafebazaarUrl
                    }
                }
            } catch (e: Exception) {
                // بی‌صدا نادیده گرفته می‌شه
            }
        }
    }

    fun dismiss() {
        _updateUrl.value = null
    }
}
