package com.michaeltroger.gruenerpass.billing

import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.billingclient.api.Purchase
import javax.inject.Inject

public interface HasBoughtProUseCase {
    public suspend operator fun invoke(): Boolean
}

internal class HasBoughtProUseCaseImpl @Inject constructor(
    private val billingRepo: BillingRepo,
    private val sharedPrefs: SharedPreferences,
) : HasBoughtProUseCase {

    override suspend operator fun invoke(): Boolean {
        if (sharedPrefs.getBoolean(PREF_KEY_HAS_BOUGHT_PRO, false)) {
            return true
        }
        val purchase = billingRepo.queryPurchase() ?: return false
        val hasPurchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        if (hasPurchased && purchase.isAcknowledged) {
            sharedPrefs.edit {
                putBoolean(PREF_KEY_HAS_BOUGHT_PRO, true)
            }
        }
        return hasPurchased
    }
}
