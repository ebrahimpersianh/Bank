package ir.sadteam.loancalc.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.sadteam.loancalc.data.prefs.UiPrefs
import ir.sadteam.loancalc.ui.components.Avatar
import ir.sadteam.loancalc.ui.components.AvatarColor
import ir.sadteam.loancalc.ui.components.AvatarShape
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * آدمکِ پروفایل (بخشِ ۳۲ فایلِ طراحی) - رجوع کن به `ui/components/Avatar.kt`.
 *
 * ⚠️ مقدارِ ذخیره‌نشده به [AvatarColor.NEUTRAL] می‌افته، نه یه رنگِ تصادفی: قاعده‌ی صریحِ کارتِ
 * `32c` («شکل تصادفی نیست»).
 */
@HiltViewModel
class AvatarViewModel @Inject constructor(private val uiPrefs: UiPrefs) : ViewModel() {
    val avatar: StateFlow<Avatar> = combine(
        uiPrefs.avatarShape,
        uiPrefs.avatarColor,
        uiPrefs.avatarPhoto,
    ) { shape, color, photo ->
        Avatar(
            shape = AvatarShape.entries.firstOrNull { it.name == shape } ?: AvatarShape.BOY,
            color = AvatarColor.entries.firstOrNull { it.name == color } ?: AvatarColor.NEUTRAL,
            photoPath = photo,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Avatar())

    fun save(value: Avatar) {
        viewModelScope.launch {
            uiPrefs.setAvatar(value.shape.name, value.color.name)
            uiPrefs.setAvatarPhoto(value.photoPath)
        }
    }
}
