package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.db.CertificateDao
import javax.inject.Inject

class InsertIntoDatabaseUseCase @Inject constructor(
    private val db: CertificateDao,
) {
    @Suppress("SpreadOperator")
    suspend operator fun invoke(certificate: Certificate, addDocumentInFront: Boolean) {
        if (addDocumentInFront) {
            db.shiftAllOrders(1)
            db.insertAll(certificate.copy(displayOrder = 0))
        } else {
            val maxOrder = db.getMaxOrder()
            val newOrder = if (maxOrder == null) {
                0
            } else {
                maxOrder + 1
            }
            db.insertAll(certificate.copy(displayOrder = newOrder))
        }
    }
}
