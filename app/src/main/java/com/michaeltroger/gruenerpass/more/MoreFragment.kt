package com.michaeltroger.gruenerpass.more

import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.michaeltroger.gruenerpass.BuildConfig
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.extensions.getInstallerPackageName
import com.michaeltroger.gruenerpass.extensions.getPackageInfo
import com.michaeltroger.gruenerpass.extensions.getSigningSubject
import com.michaeltroger.gruenerpass.pro.IsProUnlockedUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoreFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var isProUnlocked: IsProUnlockedUseCase

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.more, rootKey)

        setVersionAndInstaller()
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

    private fun setVersionAndInstaller() {
        val preference = findPreference<Preference>(
            getString(R.string.key_preference_version)
        ) ?: error("Preference is required")

        preference.title = getString(R.string.version,
            "${requireContext().getPackageInfo().versionName!!}-${BuildConfig.FLAVOR}"
        )

        preference.summary = when (requireContext().getInstallerPackageName()) {
            "com.android.vending" -> "Google Play Store"
            "com.amazon.venezia" -> "Amazon Appstore"
            "com.huawei.appmarket" -> "Huawei AppGallery"
            "org.fdroid.fdroid" -> "F-Droid"
            else -> {
                if (requireContext().getSigningSubject()?.contains("FDroid") == true) {
                    "F-Droid"
                } else null
            }
        }
    }
}
