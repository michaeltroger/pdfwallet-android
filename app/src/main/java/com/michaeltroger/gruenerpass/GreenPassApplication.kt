package com.michaeltroger.gruenerpass

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.michaeltroger.gruenerpass.cache.BitmapCache
import com.michaeltroger.gruenerpass.migration.AppMigrator
import com.michaeltroger.gruenerpass.pro.IsProUnlockedUseCase
import com.michaeltroger.gruenerpass.pro.PurchaseUpdateUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GreenPassApplication : Application() {

    @Inject
    lateinit var appMigrator: AppMigrator

    // make sure BillingClient is initiated on app start
    @Inject
    lateinit var isProUnlocked: IsProUnlockedUseCase
    @Inject
    lateinit var purchaseUpdateUseCase: PurchaseUpdateUseCase

    override fun onCreate() {
        super.onCreate()
        updateTheme()
        appMigrator.performMigration()
    }

    private fun updateTheme() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        when (prefs.getString(
            getString(R.string.key_preference_force_theme),
            getString(R.string.key_preference_theme_system)
        )) {
            getString(R.string.key_preference_theme_light) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )

            getString(R.string.key_preference_theme_dark) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )

            getString(R.string.key_preference_theme_system) -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        BitmapCache.memoryCache.evictAll()
    }
}
