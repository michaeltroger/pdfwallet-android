package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.CertificateTagCrossRef
import com.michaeltroger.gruenerpass.db.TagDao
import javax.inject.Inject

class UpdateCertificateTagsUseCase @Inject constructor(
    private val tagDao: TagDao
) {
    suspend operator fun invoke(certificateId: String, tagIds: List<Long>) {
        tagDao.deleteCrossRefsForCertificate(certificateId)
        tagIds.forEach { tagId ->
            tagDao.insertCrossRef(CertificateTagCrossRef(certificateId, tagId))
        }
    }
}
