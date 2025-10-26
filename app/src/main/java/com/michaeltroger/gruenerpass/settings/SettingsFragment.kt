package com.michaeltroger.gruenerpass.settings

import android.os.Build
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.cache.BitmapCache
import com.michaeltroger.gruenerpass.lock.AppLockedRepo
import com.michaeltroger.gruenerpass.pro.IsProUnlockedUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var biometricPromptInfo: BiometricPrompt.PromptInfo
    @Inject
    lateinit var preferenceUtil: PreferenceUtil
    @Inject
    lateinit var lockedRepo: AppLockedRepo
    @Inject
    lateinit var isProUnlocked: IsProUnlockedUseCase

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        setupBiometricSetting()
        setupForceTheme()
        setupBarcodeSetting()
        setupHalfSizeBarcodeSetting()
        setupLockscreenSetting()
        setupBrightnessSetting()
        setupPreventScreenshotsSetting()
        setupInvertPdfColorsSetting()
    }

    override fun onResume() {
        super.onResume()
        setupBilling()
    }

    private fun setupBilling() {
        val preferenceBilling = findPreference<Preference>(
            getString(R.string.key_preference_billing)
        ) ?: error("Preference is required")
        val preferenceBillingSeparator = findPreference<Preference>(
            getString(R.string.key_preference_billing_separator)
        ) ?: error("Preference is required")

        lifecycleScope.launch {
            val showUpselling = !isProUnlocked()
            preferenceBilling.isVisible = showUpselling
            preferenceBillingSeparator.isVisible = showUpselling
        }

        preferenceBilling.setOnPreferenceClickListener {
            findNavController().navigate(deepLink = "app://billing".toUri())
            true
        }
    }

    private fun setupForceTheme() {
        val preference = findPreference<DropDownPreference>(
            getString(R.string.key_preference_force_theme)
        ) ?: error("Preference is required")

        preference.setOnPreferenceChangeListener { _, newValue ->
            val valueAsString = newValue as String
            val system = getString(R.string.key_preference_theme_system)
            val light = getString(R.string.key_preference_theme_light)
            val dark = getString(R.string.key_preference_theme_dark)

            lifecycleScope.launch {
                val unlocked = isProUnlocked()
                if (newValue != system && !unlocked) {
                    findNavController().navigate(deepLink = "app://billing".toUri())
                    return@launch
                }

                when (valueAsString) {
                    system -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                    light -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                    )
                    dark -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                    )
                }
                preference.value = valueAsString
            }
            false
        }
    }

    private fun setupBarcodeSetting() {
        val preferenceBarcode = findPreference<DropDownPreference>(
            getString(R.string.key_preference_extract_barcodes)
        ) ?: error("Preference is required")

        preferenceBarcode.setOnPreferenceClickListener {
            BitmapCache.memoryCache.evictAll()
            true
        }
    }

    private fun setupHalfSizeBarcodeSetting() {
        val preference = findPreference<SwitchPreference>(
            getString(R.string.key_preference_half_size_barcodes)
        ) ?: error("Preference is required")
        preference.setOnPreferenceClickListener {
            lifecycleScope.launch {
                if (isProUnlocked()) {
                    preference.isChecked = !preference.isChecked
                } else if (preference.isChecked) {
                    preference.isChecked = false
                } else {
                    findNavController().navigate(deepLink = "app://billing".toUri())
                }
            }
            true
        }
    }

    private fun setupInvertPdfColorsSetting() {
        val preference = findPreference<SwitchPreference>(
            getString(R.string.key_preference_invert_pdf_colors)
        ) ?: error("Preference is required")
        preference.setOnPreferenceClickListener {
            lifecycleScope.launch {
                if (isProUnlocked()) {
                    preference.isChecked = !preference.isChecked
                } else if (preference.isChecked) {
                    preference.isChecked = false
                } else {
                    findNavController().navigate(deepLink = "app://billing".toUri())
                }
            }
            true
        }
    }

    private fun setupBrightnessSetting() {
        val preference = findPreference<Preference>(
            getString(R.string.key_preference_full_brightness)
        ) ?: error("Preference is required")

        preference.setOnPreferenceClickListener {
            lifecycleScope.launch {
                preferenceUtil.updateScreenBrightness(requireActivity())
            }
            true
        }
    }

    private fun setupPreventScreenshotsSetting() {
        val preference = findPreference<Preference>(
            getString(R.string.key_preference_prevent_screenshots)
        ) ?: error("Preference is required")

        preference.setOnPreferenceClickListener {
            lifecycleScope.launch {
                preferenceUtil.updatePreventScreenshots(requireActivity())
            }
            true
        }
    }

    private fun setupLockscreenSetting() {
        val preference = findPreference<Preference>(
            getString(R.string.key_preference_show_on_locked_screen)
        ) ?: error("Preference is required")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            preference.isVisible = true
            preference.setOnPreferenceClickListener {
                lifecycleScope.launch {
                    preferenceUtil.updateShowOnLockedScreen(requireActivity())
                }
                true
            }
        }
    }

    private fun setupBiometricSetting() {
        val preference = findPreference<ValidateSwitchPreference>(
            getString(R.string.key_preference_biometric)
        ) ?: error("Preference is required")

        if (BiometricManager.from(requireContext())
                .canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            preference.isVisible = true
        }

        val preventScreenshotsPreference = findPreference<SwitchPreference>(
            getString(R.string.key_preference_prevent_screenshots)
        ) ?: error("Preference is required")
        if (!preference.isChecked) {
            preventScreenshotsPreference.isEnabled = true
        }

        preference.apply {
            setOnPreferenceClickListener {
                BiometricPrompt(
                    this@SettingsFragment,
                    MyAuthenticationCallback(preference)
                ).authenticate(biometricPromptInfo)
                true
            }
        }
    }

    private inner class MyAuthenticationCallback(
        private val preference: ValidateSwitchPreference
    ) : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            lifecycleScope.launch {
                requireActivity().onUserInteraction()
                lockedRepo.unlockApp()
                preference.isChecked = !preference.isChecked

                val preventScreenshotsPreference = findPreference<SwitchPreference>(
                    getString(R.string.key_preference_prevent_screenshots)
                ) ?: error("Preference is required")

                // don't allow to disable screenshots when authentication is enabled
                // this is to prevent the app content being leaked in the "recent apps"
                if (preference.isChecked) {
                    preventScreenshotsPreference.isChecked = true
                    preventScreenshotsPreference.isEnabled = false
                    preferenceUtil.updatePreventScreenshots(requireActivity())
                } else {
                    preventScreenshotsPreference.isEnabled = true
                }
            }
        }
    }

    companion object {
        const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
