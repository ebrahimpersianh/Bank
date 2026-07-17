package ir.sadteam.loancalc.data.db

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * پسوردِ رمزنگاریِ دیتابیسِ Room (SQLCipher) - یه کلیدِ تصادفیِ ۲۵۶ بیتی که فقط یه‌بار (اولین اجرای
 * اپ) تولید می‌شه و تو یه SharedPreferences رمزنگاری‌شده با کلیدِ Android Keystore (سخت‌افزاری، هیچ‌
 * وقت از دستگاه خارج نمی‌شه و backup هم نمی‌شه) نگه‌داری می‌شه - نه خودِ پسورد تو کد هاردکده، نه جایی
 * قابل استخراجه بدون خودِ گوشیِ روت‌شده.
 */
private object DbPassphrase {
    private const val PREFS_NAME = "db_passphrase_prefs"
    private const val KEY_PASSPHRASE = "db_passphrase"

    fun getOrCreate(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        val existing = prefs.getString(KEY_PASSPHRASE, null)
        if (existing != null) return Base64.decode(existing, Base64.NO_WRAP)

        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(KEY_PASSPHRASE, Base64.encodeToString(random, Base64.NO_WRAP)).apply()
        return random
    }
}

internal fun dbPassphrase(context: Context): ByteArray = DbPassphrase.getOrCreate(context)
