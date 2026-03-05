package com.michaeltroger.gruenerpass.more

import android.os.Bundle
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.michaeltroger.gruenerpass.R
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
}
