package com.j4x.iris_ids.data.remote.interceptor

import com.j4x.iris_ids.data.local.prefs.UrlConfigStore
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Reescribe el host/puerto de cada request con la URL configurada por el admin.
 *
 * Por qué un interceptor y no recrear Retrofit:
 *   Retrofit es un singleton de Hilt (@Singleton). Recrearlo implicaría
 *   destruir y reconstruir todo el grafo de dependencias de red, lo cual es
 *   costoso y error-prone. Con este interceptor, Retrofit mantiene una URL
 *   placeholder fija (http://localhost/) y el interceptor sustituye el host
 *   en cada request leyendo el valor más reciente de UrlConfigStore.
 *   Resultado: el admin cambia la URL, la siguiente llamada ya usa el nuevo host,
 *   sin recompilar ni reiniciar la app.
 *
 * Orden en la cadena OkHttp:
 *   Debe ser el PRIMER interceptor para que AuthInterceptor y DeviceInterceptor
 *   vean la URL ya corregida, y el logging interceptor registre la URL real.
 *
 * Qué reescribe:
 *   Scheme (http/https), host e IP, y puerto. El path y query params del
 *   request original se conservan intactos.
 */
class BaseUrlInterceptor @Inject constructor(
    private val store: UrlConfigStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val configured = store.getUrl().toHttpUrlOrNull()
            ?: return chain.proceed(chain.request()) // URL inválida → dejamos pasar sin modificar

        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .scheme(configured.scheme)
            .host(configured.host)
            .port(configured.port)
            .build()

        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}
