package com.arianesanga.event.utils

import android.content.Context

object ApiKeyProvider {
    fun getOpenWeatherKey(context: Context): String {
        val appInfo = context.packageManager
            .getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
        return appInfo.metaData?.getString("OPENWEATHER_API_KEY") ?: ""
    }
}