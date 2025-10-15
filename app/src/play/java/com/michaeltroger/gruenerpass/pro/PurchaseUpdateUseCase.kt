package com.michaeltroger.gruenerpass.pro

import com.michaeltroger.gruenerpass.billing.BillingUpdateUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PurchaseUpdateUseCase @Inject constructor(private val billingUpdateUseCase: BillingUpdateUseCase) {
    operator fun invoke(): Flow<Unit> {
        return billingUpdateUseCase.invoke()
    }
}
