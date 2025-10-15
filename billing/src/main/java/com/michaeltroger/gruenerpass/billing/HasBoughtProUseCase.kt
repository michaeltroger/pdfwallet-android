package com.michaeltroger.gruenerpass.billing

import com.android.billingclient.api.Purchase
import javax.inject.Inject

public interface HasBoughtProUseCase {
    public suspend operator fun invoke(): Boolean
}

internal class HasBoughtProUseCaseImpl @Inject constructor(
    private val billingRepo: BillingRepo
) : HasBoughtProUseCase {

    override suspend operator fun invoke(): Boolean {
        val purchase = billingRepo.queryPurchases().firstOrNull() ?: return false
        val product = purchase.products.firstOrNull() ?: return false
        return purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                product == PRO_PRODUCT_ID
    }
}
