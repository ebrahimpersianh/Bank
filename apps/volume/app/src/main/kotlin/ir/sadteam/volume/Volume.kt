package ir.sadteam.volume

import android.content.Context
import android.media.AudioManager
import android.os.Build

/**
 * صدای درست را کم و زیاد می‌کند: وسطِ تماس صدای مکالمه، بقیه‌ی وقت‌ها صدای رسانه (آهنگ/ویدیو).
 * پنجره‌ی صدای خودِ گوشی هم نشان داده می‌شود تا عدد دقیق دیده شود.
 */
class Volume(context: Context) {

    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val savedBeforeMute = HashMap<Int, Int>()

    val inCall: Boolean
        get() = audio.mode == AudioManager.MODE_IN_CALL || audio.mode == AudioManager.MODE_IN_COMMUNICATION

    private val stream: Int
        get() = if (inCall) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC

    private fun min(s: Int) = if (Build.VERSION.SDK_INT >= 28) audio.getStreamMinVolume(s) else 0
    private fun max(s: Int) = audio.getStreamMaxVolume(s)

    fun current(): Int = audio.getStreamVolume(stream)

    /** کمترین پله و تعدادِ پله‌ها، برای حرکتِ پیوسته‌ی خطِ صدا زیرِ انگشت. */
    fun minIndex(): Int = min(stream)
    fun range(): Int = (max(stream) - min(stream)).coerceAtLeast(1)

    /** مقدارِ فعلی از ۰ تا ۱، برای خطِ روی قرص. */
    fun level(): Float {
        val s = stream
        val lo = min(s)
        val hi = max(s)
        if (hi <= lo) return 0f
        return (audio.getStreamVolume(s) - lo).toFloat() / (hi - lo)
    }

    fun isMuted(): Boolean {
        val s = stream
        return audio.getStreamVolume(s) <= min(s) && savedBeforeMute.containsKey(s)
    }

    fun set(index: Int) {
        val s = stream
        val v = index.coerceIn(min(s), max(s))
        if (v > min(s)) savedBeforeMute.remove(s)
        // در حالتِ «مزاحم نشوید» بعضی گوشی‌ها اجازه نمی‌دهند؛ نباید برنامه را ببندد.
        runCatching { audio.setStreamVolume(s, v, AudioManager.FLAG_SHOW_UI) }
    }

    fun step(delta: Int) = set(current() + delta)

    /** بی‌صدا ↔ برگشت به همان مقدارِ قبل. */
    fun toggleMute() {
        val s = stream
        val now = audio.getStreamVolume(s)
        if (now > min(s)) {
            savedBeforeMute[s] = now
            runCatching { audio.setStreamVolume(s, min(s), AudioManager.FLAG_SHOW_UI) }
        } else {
            val back = savedBeforeMute.remove(s) ?: ((max(s) + min(s)) / 2)
            runCatching { audio.setStreamVolume(s, back, AudioManager.FLAG_SHOW_UI) }
        }
    }
}
