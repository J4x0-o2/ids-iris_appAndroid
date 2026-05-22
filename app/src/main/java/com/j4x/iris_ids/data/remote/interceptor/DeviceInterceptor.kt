package com.j4x.iris_ids.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named

class DeviceInterceptor @Inject constructor(
    @Named("device_id") private val deviceId: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-Device-ID", deviceId)
            .build()
        return chain.proceed(request)
    }
}
