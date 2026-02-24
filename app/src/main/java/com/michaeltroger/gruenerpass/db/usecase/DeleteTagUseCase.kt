package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.TagDao
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val tagDao: TagDao
) {
    suspend operator fun invoke(id: Long) {
        tagDao.deleteById(id)
    }
}
