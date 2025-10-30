package com.michaeltroger.gruenerpass.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.michaeltroger.gruenerpass.coroutines.dispatcher.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal const val PRO_PRODUCT_ID = "pro2"
internal const val PREF_KEY_HAS_BOUGHT_PRO = "hasBoughtPro"

internal interface BillingRepo {
    val refreshPurchases: SharedFlow<Unit>
    suspend fun queryPurchase(): Purchase?
    suspend fun getProductDetails(): ProductDetails?
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails)
}

internal class BillingRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val sharedPrefs: SharedPreferences,
) : BillingRepo {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _refreshPurchases = MutableSharedFlow<Unit>(replay = 1)
    override val refreshPurchases: SharedFlow<Unit> = _refreshPurchases

    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            repositoryScope.launch {
                purchases?.firstOrNull().acknowledgePurchase()
                _refreshPurchases.emit(Unit)
            }
        }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                repositoryScope.launch {
                    _refreshPurchases.emit(Unit)
                }
            }
            override fun onBillingServiceDisconnected() {
                repositoryScope.launch {
                    _refreshPurchases.emit(Unit)
                }
            }
        })
    }

    override suspend fun queryPurchase(): Purchase? {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        val purchasesResult = withContext(ioDispatcher) {
            billingClient.queryPurchasesAsync(params.build())
        }
        purchasesResult.purchasesList.firstOrNull().acknowledgePurchase()

        return purchasesResult.purchasesList.firstOrNull()
    }

    private suspend fun Purchase?.acknowledgePurchase() = withContext(ioDispatcher) {
        if (this@acknowledgePurchase == null) return@withContext
        if (purchaseState == Purchase.PurchaseState.PURCHASED && !isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
            billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
            persistPurchase()
        }
    }

    override suspend fun getProductDetails(): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRO_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ))

        val productDetailsResult = withContext(ioDispatcher) {
            billingClient.queryProductDetails(params.build())
        }

        return productDetailsResult.productDetailsList?.firstOrNull()
    }

    override fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun persistPurchase() {
        sharedPrefs.edit {
            putBoolean(PREF_KEY_HAS_BOUGHT_PRO, true)
        }
    }
}
