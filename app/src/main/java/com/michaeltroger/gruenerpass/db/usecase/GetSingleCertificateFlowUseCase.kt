package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.CertificateDao
import com.michaeltroger.gruenerpass.db.CertificateWithTags
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetSingleCertificateFlowUseCase @Inject constructor(
    private val db: CertificateDao,
) {

    operator fun invoke(id: String): Flow<CertificateWithTags?> {
        return db.getWithTags(id)
    }
}
