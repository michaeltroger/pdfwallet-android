package com.michaeltroger.gruenerpass.navigation

import android.content.SharedPreferences
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.michaeltroger.gruenerpass.NavGraphDirections
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.lock.AppLockedRepo
import com.michaeltroger.gruenerpass.pdfimporter.PdfImporter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAutoRedirectDestinationUseCase @Inject constructor() {

    @Inject
    lateinit var lockedRepo: AppLockedRepo

    @Inject
    lateinit var pdfImporter: PdfImporter

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    operator fun invoke(navController: NavController): Flow<Result> {
        return combine(
            lockedRepo.isAppLocked(),
            pdfImporter.hasPendingFile(),
            navController.currentBackStackEntryFlow,
            ::autoRedirect
        )
    }

    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    private fun autoRedirect(
        isAppLocked: Boolean,
        hasPendingFile: Boolean,
        navBackStackEntry: NavBackStackEntry
    ): Result {
        val currentDestinationId = navBackStackEntry.destination.id
        val destination = when {
            // locked:
            isAppLocked -> {
                if (currentDestinationId == R.id.lockFragment) {
                    null
                } else {
                    NavGraphDirections.actionGlobalLockFragmentClearedBackstack()
                }
            }
            // unlocked:
            currentDestinationId == R.id.lockFragment -> {
                NavGraphDirections.actionGlobalCertificatesFragmentClearedBackstack()
            }
            currentDestinationId in listOf(
                R.id.moreFragment,
                R.id.settingsFragment,
                R.id.certificateDetailsFragment,
            ) -> {
                if (hasPendingFile) {
                    return Result.NavigateBack
                } else {
                    null
                }
            }
            currentDestinationId in listOf(
                // known issue: when on billing fragment and there is a pending file, then navigation to root view
                // is not easily possible. therefore ignore and stay. The pending file is added nevertheless
                // unless it's a password protected PDF
                R.id.billingFragment,
            ) -> {
                null
            }
            else -> {
                null // do nothing
            }
        } ?: return Result.NothingTodo

        return Result.NavigateTo(destination)
    }

    sealed class Result {
        data class NavigateTo(val navDirections: NavDirections): Result()
        data object NavigateBack: Result()
        data object NothingTodo: Result()
    }
}
