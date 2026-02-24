package com.michaeltroger.gruenerpass.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CertificateWithTags(
    @Embedded val certificate: Certificate,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CertificateTagCrossRef::class,
            parentColumn = "certificateId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
