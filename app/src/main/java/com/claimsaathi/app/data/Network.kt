package com.claimsaathi.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.claimsaathi.app.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object TokenStore {
    private lateinit var prefs: SharedPreferences
    val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun init(context: Context) {
        prefs = try {
            val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(
                context,
                "claim_saathi_auth",
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            context.getSharedPreferences("claim_saathi_auth_plain", Context.MODE_PRIVATE)
        }
    }

    var token: String?
        get() = if (::prefs.isInitialized) prefs.getString("jwt", null) else null
        set(value) {
            prefs.edit().apply {
                if (value == null) remove("jwt") else putString("jwt", value)
            }.apply()
        }
}

object Network {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        isLenient = true
    }

    private val auth = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            TokenStore.token?.let { header("Authorization", "Bearer $it") }
        }.build()
        val response = chain.proceed(request)
        if (response.code == 401) {
            TokenStore.token = null
            TokenStore.sessionExpired.tryEmit(Unit)
        }
        response
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: ClaimSaathiApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ClaimSaathiApi::class.java)

    fun apiMessage(error: Throwable): String {
        val http = error as? HttpException ?: return error.message ?: "Something went wrong"
        val raw = http.response()?.errorBody()?.string()
        val parsed = raw?.let { runCatching { json.decodeFromString<ApiErrorBody>(it).error.message }.getOrNull() }
        return parsed?.takeIf { it.isNotBlank() } ?: "Request failed (${http.code()})"
    }
}
