package ir.sadteam.loancalc.data.network

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * همون سروری که کلاینت وب فعلی بهش وصل می‌شه (index.html: API_BASE_URL) — VPS آروان‌کلاود،
 * nginx reverse-proxy روی Node/Express (server/). جزئیات دیپلوی تو CLAUDE.md/server/README.md.
 */
private const val BASE_URL =
    "https://server-8a887fd3-2459-4e6d-ace8-884dc36ca3db.ir-thr-fr1.arvancompute.ir/"

object ApiClient {
    fun create(baseUrl: String = BASE_URL): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
            .create(ApiService::class.java)
    }
}
