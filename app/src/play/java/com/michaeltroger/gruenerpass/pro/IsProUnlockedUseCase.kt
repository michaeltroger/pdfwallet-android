package com.michaeltroger.gruenerpass.pro

import com.michaeltroger.gruenerpass.billing.HasBoughtProUseCase
import javax.inject.Inject

class IsProUnlockedUseCase @Inject constructor(private val hasBoughtPro: HasBoughtProUseCase) {

    suspend operator fun invoke(): Boolean {
        return hasBoughtPro()
    }
}
