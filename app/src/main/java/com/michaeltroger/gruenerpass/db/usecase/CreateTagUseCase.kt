package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.db.TagDao
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val tagDao: TagDao
) {
    suspend operator fun invoke(name: String): Long {
        return tagDao.insert(Tag(name = name))
    }
}
