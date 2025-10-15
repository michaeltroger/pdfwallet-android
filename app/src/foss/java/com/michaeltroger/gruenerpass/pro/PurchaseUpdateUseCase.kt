package com.michaeltroger.gruenerpass.pro

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class PurchaseUpdateUseCase @Inject constructor() {
    operator fun invoke(): Flow<Unit> {
        return flowOf(Unit)
    }
}
