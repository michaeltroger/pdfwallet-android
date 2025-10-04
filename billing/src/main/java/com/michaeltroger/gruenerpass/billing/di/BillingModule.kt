package com.michaeltroger.gruenerpass.billing.di

import com.michaeltroger.gruenerpass.billing.BillingRepo
import com.michaeltroger.gruenerpass.billing.BillingRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class BillingModule {

    @Binds
    @Singleton
    internal abstract fun billingModule(
        impl: BillingRepoImpl
    ): BillingRepo
}
