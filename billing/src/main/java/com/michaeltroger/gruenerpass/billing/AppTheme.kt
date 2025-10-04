package com.michaeltroger.gruenerpass.billing

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
internal fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(colorScheme),
        content = content
    )
}

@Composable
private fun appTypography(colorScheme: androidx.compose.material3.ColorScheme) = Typography(
    displayLarge = Typography().displayLarge.copy(color = colorScheme.onBackground),
    displayMedium = Typography().displayMedium.copy(color = colorScheme.onBackground),
    displaySmall = Typography().displaySmall.copy(color = colorScheme.onBackground),
    headlineLarge = Typography().headlineLarge.copy(color = colorScheme.onBackground),
    headlineMedium = Typography().headlineMedium.copy(color = colorScheme.onBackground),
    headlineSmall = Typography().headlineSmall.copy(color = colorScheme.onBackground),
    titleLarge = Typography().titleLarge.copy(color = colorScheme.onBackground),
    titleMedium = Typography().titleMedium.copy(color = colorScheme.onBackground),
    titleSmall = Typography().titleSmall.copy(color = colorScheme.onBackground),
    bodyLarge = Typography().bodyLarge.copy(color = colorScheme.onBackground),
    bodyMedium = Typography().bodyMedium.copy(color = colorScheme.onBackground),
    bodySmall = Typography().bodySmall.copy(color = colorScheme.onBackground),
    labelLarge = Typography().labelLarge.copy(color = colorScheme.onBackground),
    labelMedium = Typography().labelMedium.copy(color = colorScheme.onBackground),
    labelSmall = Typography().labelSmall.copy(color = colorScheme.onBackground),
)
