package ir.sadteam.loancalc.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * یه پیامِ مرکزِ پیام‌ها - بخشِ ۴۰ طراحی (فریم‌های `40a`/`40b`/`40c`).
 *
 * **قاعده‌ی مرکزیِ طرح: دو دسته، نه شش.** شش [Kind] وجود داره ولی رفتارشون فقط دو جوره:
 * - **اقدام‌دار** ([Kind.DETECTED_TX]، [Kind.LOAN_DUE]): با کشیدن بسته نمی‌شه، خودبه‌خود پاک
 *   نمی‌شه، و **فقط این‌ها** تو شمارنده‌ی عددیِ زنگ حساب می‌شن.
 * - **خبر** (بقیه): کشیدن = خوانده شد، بعدِ ۳۰ روز پاک می‌شه، و فقط یه نقطه‌ی سبز می‌گیره نه عدد.
 *
 * **منبعِ واحد** (تاکیدِ صریحِ طرح): پیام **اول اینجا** ساخته می‌شه، بعد اعلانِ گوشی از روش
 * ساخته می‌شه - نه برعکس. پس بستنِ پیام تو اپ یعنی اعلانِ گوشی هم باید cancel بشه.
 */
@Entity(tableName = "inbox_messages")
data class InboxMessageEntity(
    @PrimaryKey val id: Long,
    /** یکی از مقادیرِ [Kind]. به‌شکلِ رشته ذخیره می‌شه تا افزودنِ نوعِ تازه مهاجرت نخواد. */
    val kind: String,
    val title: String,
    val body: String,
    /** میلی‌ثانیه‌ی epoch - برای گروه‌بندیِ «امروز / این هفته / قدیمی‌تر» و قاعده‌ی ۳۰ روز. */
    val createdAt: Long,
    /** `null` یعنی خوانده‌نشده. */
    val readAt: Long? = null,
    /**
     * فقط برای اقدام‌دارها: `OPEN` تا وقتی کاربر تصمیم بگیره، بعد `DONE` یا `DISMISSED`.
     * برای خبرها همیشه `NONE`.
     */
    val actionState: String = ActionState.NONE,
    /** شناسه‌ی چیزی که پیام بهش اشاره می‌کنه - مثلاً id تراکنشِ تاییدنشده یا id وام. */
    val refId: String? = null,
) {
    object Kind {
        /** تراکنشی که خودکار از پیامک/اعلان خونده شده و **منتظرِ تاییده**. */
        const val DETECTED_TX = "DETECTED_TX"
        const val LOAN_DUE = "LOAN_DUE"
        const val BUDGET_ALERT = "BUDGET_ALERT"
        const val REWARD = "REWARD"
        const val STREAK_REMINDER = "STREAK_REMINDER"
        const val SYSTEM = "SYSTEM"

        /** همون «دو دسته»ی طرح - هر جا رفتار فرق می‌کنه از این استفاده کن، نه از لیستِ دستی. */
        fun isActionable(kind: String): Boolean = kind == DETECTED_TX || kind == LOAN_DUE
    }

    object ActionState {
        const val NONE = "NONE"
        const val OPEN = "OPEN"
        const val DONE = "DONE"
        const val DISMISSED = "DISMISSED"
    }
}
