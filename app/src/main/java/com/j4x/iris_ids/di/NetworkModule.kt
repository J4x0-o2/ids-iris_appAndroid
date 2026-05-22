package com.j4x.iris_ids.di

import com.j4x.iris_ids.data.local.prefs.DeviceIdManager
import com.j4x.iris_ids.data.remote.api.IrisApi
import com.j4x.iris_ids.data.remote.interceptor.AuthInterceptor
import com.j4x.iris_ids.data.remote.interceptor.BaseUrlInterceptor
import com.j4x.iris_ids.data.remote.interceptor.DeviceInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * URL placeholder requerida por Retrofit para construir la instancia.
     * BaseUrlInterceptor la sobreescribe en cada request con la URL que el
     * admin configure desde el panel (UrlConfigStore). Nunca se usa directamente
     * en producción; sólo sirve como valor sintácticamente válido para Retrofit.
     */
    private const val PLACEHOLDER_URL = "http://localhost/"

    @Provides
    @Singleton
    @Named("device_id")
    fun provideDeviceId(manager: DeviceIdManager): String = manager.getOrCreate()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        baseUrlInterceptor: BaseUrlInterceptor,
        authInterceptor: AuthInterceptor,
        deviceInterceptor: DeviceInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        // BaseUrlInterceptor primero: reescribe el host antes de que los demás
        // interceptores añadan headers o el logger registre la URL final.
        .addInterceptor(baseUrlInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(deviceInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideIrisApi(retrofit: Retrofit): IrisApi = retrofit.create(IrisApi::class.java)
}
