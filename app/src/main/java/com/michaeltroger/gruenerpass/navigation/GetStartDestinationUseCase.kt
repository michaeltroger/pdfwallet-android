package com.michaeltroger.gruenerpass.navigation;

import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.lock.AppLockedRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetStartDestinationUseCase @Inject constructor() {

    @Inject
    lateinit var lockedRepo: AppLockedRepo

    suspend operator fun invoke(): Int {
        return lockedRepo.isAppLocked().map { isAppLocked ->
            if (isAppLocked) {
                R.id.lockFragment
            } else {
                R.id.certificatesFragment
            }
        }.first()
    }
}
