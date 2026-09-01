package ir.sadteam.loancalc.core

/** روشِ تقسیمِ یه رویدادِ «دنگ» - فریمِ `22c`، جوابِ سوالِ ۷ی
 * design/MESSAGE-round4-to-design.md: هر ۴ روش از نسخه‌ی اول. */
enum class DangMethod(val label: String) {
    EQUAL("مساوی"),
    PERCENTAGE("درصدی"),
    CUSTOM("دلخواه"),
    ITEMIZED("قلم‌به‌قلم"),
}
