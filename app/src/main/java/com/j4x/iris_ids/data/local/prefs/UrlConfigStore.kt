package com.j4x.iris_ids.data.local.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste la URL base del servidor Go en SharedPreferences.
 *
 * Por qué SharedPreferences y no DataStore:
 *   La URL se lee de forma síncrona dentro de BaseUrlInterceptor (que corre en el
 *   hilo de IO de OkHttp). DataStore es asíncrono (suspend / Flow) y no puede
 *   usarse directamente desde un Interceptor. SharedPreferences, en cambio, lee
 *   desde memoria en el hilo actual sin bloquear.
 *
 * Por qué no está hardcodeada en NetworkModule:
 *   12 tablets en campo pueden apuntar a servidores distintos o a una IP que
 *   cambia. Configurarla desde el panel de admin evita recompilar y redesplegar
 *   la APK cada vez que cambia la IP del servidor en la red WiFi local.
 */
@Singleton
class UrlConfigStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        context.getSharedPreferences("iris_config", Context.MODE_PRIVATE)
    }

    fun getUrl(): String = prefs.getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL

    fun setUrl(url: String) = prefs.edit().putString(KEY_URL, url).apply()

    companion object {
        private const val KEY_URL = "base_url"

        // Fallback inicial. Sobreescribir desde AdminLogin → ⚙ antes de usar la app.
        const val DEFAULT_URL = "http://192.168.1.110:8090/"
    }
}
