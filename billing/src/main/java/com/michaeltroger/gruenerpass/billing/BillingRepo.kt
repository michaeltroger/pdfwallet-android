package com.michaeltroger.gruenerpass.billing

import android.app.Activity
import android.content.Context
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

internal interface BillingRepo {
    val refreshPurchases: SharedFlow<Unit>
    suspend fun queryPurchases(): List<Purchase>
    suspend fun getAvailableProducts(): List<ProductDetails>
    suspend fun getProductDetails(productId: String): ProductDetails?
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails)
}

internal val productOrderList = listOf(
    "small_supporter",
    "medium_supporter",
    "big_supporter",
    "mega_supporter",
    "ultimate_supporter"
)

private val productList = listOf(
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId("small_supporter")
        .setProductType(BillingClient.ProductType.INAPP)
        .build(),
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId("medium_supporter")
        .setProductType(BillingClient.ProductType.INAPP)
        .build(),
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId("big_supporter")
        .setProductType(BillingClient.ProductType.INAPP)
        .build(),
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId("mega_supporter")
        .setProductType(BillingClient.ProductType.INAPP)
        .build(),
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId("ultimate_supporter")
        .setProductType(BillingClient.ProductType.INAPP)
        .build(),
)

internal class BillingRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BillingRepo {
    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _refreshPurchases = MutableSharedFlow<Unit>(replay = 1)
    override val refreshPurchases: SharedFlow<Unit> = _refreshPurchases

    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            repositoryScope.launch {
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
                    // ignore
            }
        })
    }

    public override suspend fun queryPurchases(): List<Purchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)

        val purchasesResult = withContext(ioDispatcher) {
            billingClient.queryPurchasesAsync(params.build())
        }

        return purchasesResult.purchasesList
    }

    public override suspend fun getAvailableProducts(): List<ProductDetails> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)

        val productDetailsResult = withContext(ioDispatcher) {
            billingClient.queryProductDetails(params.build())
        }

        return productDetailsResult.productDetailsList ?: emptyList()
    }

    override suspend fun getProductDetails(productId: String): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ))

        val productDetailsResult = withContext(ioDispatcher) {
            billingClient.queryProductDetails(params.build())
        }

        return productDetailsResult.productDetailsList?.firstOrNull()
    }

    public override fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
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
}
