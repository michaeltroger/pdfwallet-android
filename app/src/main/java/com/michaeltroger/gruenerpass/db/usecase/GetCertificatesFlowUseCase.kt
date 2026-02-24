package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.CertificateWithTags
import com.michaeltroger.gruenerpass.db.CertificateDao
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCertificatesFlowUseCase @Inject constructor(
    private val db: CertificateDao,
) {

    operator fun invoke(): Flow<List<CertificateWithTags>> {
        return db.getAllWithTags()
    }
}
