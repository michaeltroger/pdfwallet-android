package com.michaeltroger.gruenerpass.billing

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun BillingComposable(
    viewModel: BillingViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current
    val state = viewModel.state.collectAsState().value
    BillingComposableImpl(
        state = state,
        launchBillingFlow = {
            if (activity == null) return@BillingComposableImpl
            viewModel.launchBillingFlow(activity, it)
        })
}

@Composable
private fun BillingComposableImpl(
    state: BillingState,
    launchBillingFlow: (com.android.billingclient.api.ProductDetails) -> Unit,
) {
    LazyColumn(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(AppPadding.m)
    ) {
        if (state.processing) {
            item {
                Text(
                    text = stringResource(R.string.billing_purchases_processing),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = AppPadding.l),
                )
            }
        }
        if (state.purchases.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.billing_purchases_thanks),
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    modifier = Modifier.padding(bottom = AppPadding.l)
                )
                Text(text = stringResource(R.string.billing_purchases_title))
            }
        }
        items(items = state.purchases) {
            Column(Modifier.padding(AppPadding.m)) {
                Text(stringResource(R.string.billing_purchases_date)  + " ${it.purchaseTime}")
                Text(stringResource(R.string.billing_purchases_order_id) + " ${it.orderId}")
                Text(stringResource(R.string.billing_purchases_item) +" ${it.productName}")
            }
        }
        if (state.purchases.isNotEmpty()) {
            item {
                Spacer(Modifier.height(AppPadding.l))
            }
        }
        item {
            Column {
                Text(stringResource(R.string.billing_benefits_description))
                BulletedList(listOf(
                    stringResource(R.string.billing_benefit_1),
                    stringResource(R.string.billing_benefit_2),
                    stringResource(R.string.billing_benefit_3)
                ))
                Spacer(Modifier.height(AppPadding.l))
            }
        }
        items(items = state.productDetails) {
            Card {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = {
                                launchBillingFlow(it.productDetails ?: return@clickable)
                            },
                        )
                        .padding(AppPadding.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(1f)
                    ) {
                        Text(
                            text = it.name,
                            fontWeight = FontWeight.Bold
                        )
                        Text(it.description)
                        Text(it.price ?: "")
                    }
                    Text(
                        text = stringResource(R.string.billing_product_buy),
                        modifier = Modifier.padding(start = AppPadding.m),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
@DualThemePreview
private fun PreviewEmpty() {
    AppTheme {
        BillingComposableImpl(
            state = BillingState(false, emptyList(), emptyList()),
            launchBillingFlow = {})
    }
}

@Composable
@DualThemePreview
private fun PreviewNormal() {
    AppTheme {
        BillingComposableImpl(
            state = BillingState(
                processing = true,
                purchases = listOf(
                    Purchase(
                        orderId = "1saf23-23323-sffaf2",
                        productName = "Small Supporter",
                        purchaseTime = "12.12.2023 12:12"
                    ),
                ),
                productDetails = listOf(
                    ProductDetails(
                        name = "Small Supporter",
                        description = "Little support for the developer",
                        productDetails = null,
                        price = "2 Euro"
                    )
                )
            ),
            launchBillingFlow = {}
        )
    }
}
