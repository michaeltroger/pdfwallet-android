package com.michaeltroger.gruenerpass.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["certificateId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Certificate::class,
            parentColumns = ["id"],
            childColumns = ["certificateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CertificateTagCrossRef(
    val certificateId: String,
    val tagId: Long
)
