package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.CertificateDao
import javax.inject.Inject

class ChangeCertificateOrderUseCase @Inject constructor(
    private val db: CertificateDao,
) {
    @Suppress("SpreadOperator")
    suspend operator fun invoke(sortedIdList: List<String>) {
        sortedIdList.forEachIndexed { index, id ->
            db.updateOrder(
                id = id,
                order = index
            )
        }
    }
}
