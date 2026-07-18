# قوانین R8/ProGuard - چون این اپ خیلی جاها Gson رو مستقیم و از رو reflection (TypeToken، بدون
# @SerializedName) رو کلاس‌های داده (Room entities تو data/db + Map<String,Any?> برای فرمت وب) صدا
# می‌زنه، بدونِ این قوانین minify اسم فیلدها رو عوض می‌کنه و JSON خروجی/ورودیِ پشتیبان‌گیری بی‌صدا
# خراب می‌شه (نه کرش - سکوت! چون Gson فیلدهای گم‌شده رو فقط null می‌ذاره). رجوع کن به
# ChequeRepository/AccountRepository/LoanRepository/AuthRepository (همه‌شون exportBackupJson/
# importBackupJson دارن).

# --- Gson: خودِ generic signature ها و annotation های reflection رو نگه دار ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# TypeToken های ناشناس (object : TypeToken<...>() {}) باید سیگنیچرشون سالم بمونه.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# --- کلاس‌های داده‌ی خودِ اپ که مستقیم با Gson (de)serialize می‌شن (Room entities + backup) ---
# اسم و نوعِ فیلدها باید عیناً بمونه، وگرنه JSON پشتیبان‌گیری/بازیابی و همگام‌سازی با فرمت وب می‌شکنه.
-keep class ir.sadteam.loancalc.data.db.** { *; }
-keep class ir.sadteam.loancalc.core.** { *; }
# DTOهای درخواست/پاسخِ Retrofit+Gson (ApiService.kt: OTP، همگام‌سازی وام، بک‌آپ چک/حساب، نرخ بانک‌ها) -
# هیچ‌کدوم @SerializedName ندارن و رو تطبیقِ اسمِ فیلد با کلیدِ JSON سرور تکیه می‌کنن؛ بدونِ این قانون
# minify اسم فیلدها رو عوض می‌کنه و همه‌ی این درخواست‌ها بی‌صدا (بدون کرش) با مقادیرِ null جواب می‌گیرن.
-keep class ir.sadteam.loancalc.data.network.** { *; }

# --- Retrofit/OkHttp (طبق راهنمای رسمی Retrofit برای R8) ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keep,allowobfuscation interface ir.sadteam.loancalc.data.network.ApiService
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# --- Room: خودِ کتابخونه consumer-rules خودشو داره؛ فقط DAO ها رو صریح نگه می‌داریم که proguard
# لایه‌ی abstract/تولیدشده‌ی Room رو اشتباهی حذف نکنه. ---
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# --- Hilt/Dagger: خودشون consumer-rules دارن؛ فقط کلاس‌های @HiltViewModel/@HiltWorker رو صریح
# نگه می‌داریم چون از رو نام تزریق می‌شن. ---
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @androidx.hilt.work.HiltWorker class *

# --- SQLCipher: JNI/reflection داخلی داره، کلاس‌هاش نباید عوض/حذف بشن. ---
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# --- myket-billing-client (فلیورِ myket): برخلافِ Poolakey که خودش consumer-rules بسته‌بندی‌شده
# داره، این کتابخونه از الگوی قدیمیِ IAB v3 (AIDL + کلاسِ استابِ سرویس تولیدشده) استفاده می‌کنه که
# R8 بدونِ keep صریح ممکنه اسم/امضاش رو عوض کنه و باندشدن به سرویسِ مایکت رو زمانِ اجرا بشکنه. ---
-keep class ir.myket.billingclient.** { *; }
-dontwarn ir.myket.billingclient.**
