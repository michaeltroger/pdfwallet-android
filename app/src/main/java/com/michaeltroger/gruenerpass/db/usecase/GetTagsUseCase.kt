package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.db.TagDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val tagDao: TagDao
) {
    operator fun invoke(): Flow<List<Tag>> {
        return tagDao.getAll()
    }
}
