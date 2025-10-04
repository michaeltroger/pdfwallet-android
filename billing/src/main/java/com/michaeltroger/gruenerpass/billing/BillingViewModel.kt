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
    private val _state = MutableStateFlow(BillingState(emptyList(), emptyList()))
    internal val state: StateFlow<BillingState> = _state

    init {
        viewModelScope.launch {
            billingRepo.refreshPurchases.onEach {
                val billingState = BillingState(
                    purchases = billingRepo.queryPurchases().sortedByDescending { it.purchaseTime }.mapNotNull {
                        val productId = it.products.firstOrNull() ?: return@mapNotNull null
                        val productDetails = billingRepo.getProductDetails(productId) ?: return@mapNotNull null
                        Purchase(
                            orderId = it.orderId,
                            productName = productDetails.name,
                            purchaseTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                                .withZone(ZoneId.systemDefault())
                                .format(Instant.ofEpochMilli(it.purchaseTime))
                        )
                    },
                    productDetails = billingRepo.getAvailableProducts().sortedBy { productDetail ->
                        productOrderList.indexOf(productDetail.productId)
                    }.map {
                        ProductDetails(
                            productDetails = it,
                            name = it.name,
                            description = it.description,
                            price = it.oneTimePurchaseOfferDetails?.formattedPrice
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
    val purchases: List<Purchase>,
    val productDetails: List<ProductDetails>
)

internal data class Purchase(
    val orderId: String?,
    val productName: String?,
    val purchaseTime: String,
)

internal data class ProductDetails(
    val productDetails: com.android.billingclient.api.ProductDetails?,
    val name: String,
    val description: String,
    val price: String?,
)
