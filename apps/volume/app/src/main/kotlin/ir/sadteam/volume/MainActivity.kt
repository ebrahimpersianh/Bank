package ir.sadteam.volume

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * صفحه‌ی اول: فهرستِ اجازه‌ها (هر کدام یک دکمه، با تیک وقتی انجام شد) و دکمه‌ی روشن/خاموش.
 * همه‌ی اجازه‌ها اولِ کار گرفته می‌شوند تا دکمه هیچ‌وقت بسته نشود و بعد از روشن‌شدنِ گوشی بالا بیاید.
 */
class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var list: LinearLayout
    private lateinit var main: Button
    private lateinit var status: TextView
    private lateinit var showMe: Button
    private var batteryTaps = 0
    private val autostartSeen get() = getSharedPreferences("volume", Context.MODE_PRIVATE)
    private val density get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * density).toInt()

    private val green = Color.rgb(14, 169, 104)
    private val ink = Color.rgb(22, 32, 28)
    private val muted = Color.rgb(93, 107, 101)
    private val line = Color.rgb(214, 224, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        window.statusBarColor = Color.rgb(238, 243, 241)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(20), dp(28), dp(20), dp(28))
            setBackgroundColor(Color.rgb(238, 243, 241))
        }
        root.addView(text("ولوم", 26f, ink, bold = true))
        root.addView(text("دکمه‌ی شناورِ صدا، جای دکمه‌های کناریِ گوشی.", 14f, muted).apply { setPadding(0, dp(4), 0, dp(20)) })
        root.addView(text("یک بار این‌ها را روشن کن تا دکمه همیشه بماند و بعد از خاموش‌وروشن‌شدنِ گوشی خودش بیاید:", 14f, ink).apply { setPadding(0, 0, 0, dp(12)) })

        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list)

        main = Button(this).apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = round(green, dp(16))
            setOnClickListener { toggleMain() }
        }
        root.addView(main, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)).apply { topMargin = dp(20) })

        // ۱۳ آبان (گوشیِ کاربر: «معلوم نیست کجاست»): وضعیت + دکمه‌ی بیرون‌آوردنِ قرص.
        status = text("", 13.5f, muted).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, 0) }
        root.addView(status, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        showMe = Button(this).apply {
            text = "نشانم بده کجاست"
            textSize = 15f
            isAllCaps = false
            setTextColor(green)
            background = round(Color.WHITE, dp(16), green)
            setOnClickListener { OverlayService.show(this@MainActivity) }
        }
        root.addView(showMe, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)).apply { topMargin = dp(10) })

        root.addView(
            text(
                "طرزِ کار: کشیدنِ بالا و پایین صدا را کم و زیاد می‌کند. زدن روی نیمه‌ی بالا یا پایین، یک پله. " +
                    "دو بار زدن، بی‌صدا. نگه‌داشتن تا بلرزد، بعد هر جا بکشی جابه‌جا می‌شود. " +
                    "اگر ۵ ثانیه دست نزنی کنارِ صفحه می‌رود؛ باریکه‌اش را بزن تا برگردد. وسطِ تماس صدای مکالمه را عوض می‌کند.",
                13f, muted,
            ).apply { setPadding(0, dp(18), 0, 0); setLineSpacing(0f, 1.35f) },
        )

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(238, 243, 241))
            addView(root)
        })
    }

    override fun onResume() {
        super.onResume()
        render()
        // اجازه‌ی اصلی داده شده و کاربر خاموشش نکرده → همین حالا روشن شود.
        if (Setup.overlayOk(this) && prefs.enabled) OverlayService.start(this)
    }

    private fun render() {
        list.removeAllViews()
        row(
            "نمایش روی برنامه‌های دیگر", "بدونِ این، دکمه روی صفحه نمی‌آید.",
            Setup.overlayOk(this),
        ) { startActivity(Setup.overlayIntent(this)) }
        row(
            "اعلانِ کوچکِ ثابت", "اندروید فقط با این اعلان می‌گذارد دکمه همیشه بماند.",
            Setup.notifOk(this),
        ) { if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1) }
        row(
            "بدونِ محدودیتِ باتری", "تا گوشی برای صرفه‌جویی، دکمه را نبندد.",
            Setup.batteryOk(this),
        ) { Setup.openBattery(this, preferList = batteryTaps++ > 0) }
        val autoDone = autostartSeen.getBoolean("autostart_opened", false)
        row(
            "اجرای خودکار", "تا بعد از خاموش‌وروشن‌کردنِ گوشی خودش بیاید. در صفحه‌ای که باز می‌شود «ولوم» را روشن کن.",
            autoDone, manual = true,
        ) {
            autostartSeen.edit().putBoolean("autostart_opened", true).apply()
            Setup.openAutostart(this)
        }
        if (Setup.isXiaomi()) {
            val popDone = autostartSeen.getBoolean("popup_opened", false)
            row(
                "پنجره در پس‌زمینه (شیائومی)", "در صفحه‌ای که باز می‌شود «نمایشِ پنجره‌های بازشو هنگامِ اجرا در پس‌زمینه» و «نمایش روی صفحه‌ی قفل» را روشن کن.",
                popDone, manual = true,
            ) {
                autostartSeen.edit().putBoolean("popup_opened", true).apply()
                Setup.openXiaomiPopup(this)
            }
        }
        val on = prefs.enabled && Setup.overlayOk(this)
        main.text = if (on) "خاموش کردنِ دکمه" else "روشن کردنِ دکمه"
        main.background = round(if (on) Color.rgb(120, 132, 127) else green, dp(16))
        showMe.visibility = if (on) View.VISIBLE else View.GONE
        status.removeCallbacks(statusTick)
        status.postDelayed(statusTick, 600)
    }

    /** وضعیتِ واقعیِ قرص (سرویس کمی بعد از روشن‌شدن بالا می‌آید، پس با تأخیر خوانده می‌شود). */
    private val statusTick = Runnable {
        val on = prefs.enabled && Setup.overlayOk(this)
        status.text = when {
            !Setup.overlayOk(this) -> "اول «نمایش روی برنامه‌های دیگر» را روشن کن."
            !on -> "دکمه خاموش است."
            OverlayService.showing -> "دکمه روی صفحه است؛ اگر نمی‌بینی‌اش، کنارِ صفحه رفته. «نشانم بده» را بزن."
            OverlayService.lastError != null -> "گوشی اجازه‌ی نمایشِ دکمه را نداد. ردیف‌های بالا را دوباره نگاه کن."
            else -> "در حالِ روشن‌شدن…"
        }
    }

    private fun toggleMain() {
        if (!Setup.overlayOk(this)) {
            startActivity(Setup.overlayIntent(this))
            return
        }
        if (prefs.enabled) {
            prefs.enabled = false
            OverlayService.stop(this)
        } else {
            prefs.enabled = true
            // روشن‌کردن = همان لحظه وسطِ لبه بیرون بیاید تا کاربر ببیند کجاست.
            OverlayService.show(this)
        }
        render()
    }

    /** یک ردیفِ اجازه: عنوان و توضیح، و سمتِ چپ یا تیکِ سبز یا دکمه‌ی «روشن کن». */
    private fun row(title: String, desc: String, done: Boolean, manual: Boolean = false, action: () -> Unit) {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = round(Color.WHITE, dp(16), line)
        }
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(text(title, 15f, ink, bold = true))
        texts.addView(text(desc, 12.5f, muted).apply { setPadding(0, dp(2), 0, 0); setLineSpacing(0f, 1.25f) })
        r.addView(texts, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        val right: View = if (done && !manual) {
            text("✓", 20f, green, bold = true).apply { gravity = Gravity.CENTER }
        } else {
            Button(this).apply {
                text = if (manual && done) "دوباره" else if (manual) "باز کن" else "روشن کن"
                textSize = 13f
                isAllCaps = false
                setTextColor(if (manual && done) green else Color.WHITE)
                background = if (manual && done) round(Color.WHITE, dp(12), green) else round(green, dp(12))
                minWidth = 0; minimumWidth = 0
                setPadding(dp(14), 0, dp(14), 0)
                setOnClickListener { action() }
            }
        }
        r.addView(right, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(40)).apply { marginStart = dp(10) })
        list.addView(r, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) })
    }

    private fun text(s: String, size: Float, color: Int, bold: Boolean = false) = TextView(this).apply {
        text = s
        textSize = size
        setTextColor(color)
        textDirection = View.TEXT_DIRECTION_RTL
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun round(fill: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = radius.toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        render()
    }
}
