package com.michaeltroger.gruenerpass.billing

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
internal class BillingViewModel @Inject constructor(
    private val billingRepo: BillingRepo
) : ViewModel() {
    private val _state = MutableStateFlow(
        BillingState(
            processing = false,
            purchase = null,
            productDetails = null,
        )
    )
    internal val state: StateFlow<BillingState> = _state

    init {
        viewModelScope.launch {
            billingRepo.refreshPurchases.onEach { unit: Unit ->
                val viewPurchase = billingRepo.queryPurchase()?.let { purchase: com.android.billingclient.api.Purchase ->
                    Purchase(
                        orderId = purchase.orderId,
                        purchaseTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochMilli(purchase.purchaseTime))
                    )
                }
                val productDetails = billingRepo.getProductDetails()
                val billingState = BillingState(
                    processing = viewPurchase != null && viewPurchase.orderId == null,
                    purchase = viewPurchase.takeIf { it?.orderId != null },
                    productDetails = if (productDetails  == null) {
                        null
                    } else {
                        ProductDetails(
                            productDetails = productDetails,
                            name = productDetails.name,
                            description = productDetails.description,
                            price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
                        )
                    }
                )
                _state.emit(billingState)
            }.collect()
        }
    }

    internal fun launchBillingFlow(activity: Activity, productDetails: com.android.billingclient.api.ProductDetails) {
        billingRepo.launchBillingFlow(activity, productDetails)
    }
}

internal data class BillingState(
    val processing: Boolean,
    val purchase: Purchase?,
    val productDetails: ProductDetails?
)

internal data class Purchase(
    val orderId: String?,
    val purchaseTime: String,
)

internal data class ProductDetails(
    val productDetails: com.android.billingclient.api.ProductDetails?,
    val name: String,
    val description: String,
    val price: String?,
)
