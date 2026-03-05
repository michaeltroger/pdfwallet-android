package com.michaeltroger.gruenerpass.more

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.ComposeView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.michaeltroger.gruenerpass.R

class FooterPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val composeView = holder.itemView.findViewById<ComposeView>(R.id.footer_compose)
        composeView.setContent {
            MaterialTheme(if (isSystemInDarkTheme()) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }) {
                FooterComposable()
            }
        }
    }
}
