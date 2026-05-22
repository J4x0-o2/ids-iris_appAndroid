package com.j4x.iris_ids.data.local.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        context.getSharedPreferences("iris_device", Context.MODE_PRIVATE)
    }

    fun getOrCreate(): String =
        prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }

    companion object {
        private const val KEY = "device_id"
    }
}
