package com.michaeltroger.gruenerpass.billing

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

public interface BillingUpdateUseCase {
    public operator fun invoke(): Flow<Unit>
}

internal class BillingUpdateUseCaseImpl @Inject constructor(
    private val billingRepo: BillingRepo
) : BillingUpdateUseCase {

    override operator fun invoke(): Flow<Unit> {
        return billingRepo.refreshPurchases
    }
}
