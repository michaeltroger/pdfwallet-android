package com.michaeltroger.gruenerpass.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.michaeltroger.gruenerpass.NavGraphDirections
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.lock.AppLockedRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAutoRedirectDestinationUseCase @Inject constructor() {

    @Inject
    lateinit var lockedRepo: AppLockedRepo

    operator fun invoke(navController: NavController): Flow<Result> {
        return combine(
            lockedRepo.isAppLocked(),
            navController.currentBackStackEntryFlow.map { it.destination.id },
            ::autoRedirect
        )
    }

    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    private fun autoRedirect(
        isAppLocked: Boolean,
        currentDestinationId: Int
    ): Result {
        val destination = if (isAppLocked) {
            if (currentDestinationId == R.id.lockFragment) {
                null
            } else {
                NavGraphDirections.actionGlobalLockFragmentClearedBackstack()
            }
        } else { // unlocked
            if (currentDestinationId == R.id.lockFragment) {
                NavGraphDirections.actionGlobalCertificatesFragmentClearedBackstack()
            } else {
                null
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
