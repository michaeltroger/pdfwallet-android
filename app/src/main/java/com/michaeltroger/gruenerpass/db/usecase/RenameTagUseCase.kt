package com.michaeltroger.gruenerpass.db.usecase

import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.db.TagDao
import javax.inject.Inject

class RenameTagUseCase @Inject constructor(
    private val tagDao: TagDao
) {
    suspend operator fun invoke(id: Long, newName: String) {
        tagDao.update(Tag(id = id, name = newName))
    }
}
