package com.michaeltroger.gruenerpass.billing.di

import com.michaeltroger.gruenerpass.billing.BillingRepo
import com.michaeltroger.gruenerpass.billing.BillingRepoImpl
import com.michaeltroger.gruenerpass.billing.BillingUpdateUseCase
import com.michaeltroger.gruenerpass.billing.BillingUpdateUseCaseImpl
import com.michaeltroger.gruenerpass.billing.HasBoughtProUseCase
import com.michaeltroger.gruenerpass.billing.HasBoughtProUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public abstract class BillingModule {

    @Binds
    @Singleton
    internal abstract fun proUseCase(
        impl: HasBoughtProUseCaseImpl
    ): HasBoughtProUseCase

    @Binds
    @Singleton
    internal abstract fun billingUpdateUseCase(
        impl: BillingUpdateUseCaseImpl
    ): BillingUpdateUseCase

    @Binds
    @Singleton
    internal abstract fun billingModule(
        impl: BillingRepoImpl
    ): BillingRepo
}
