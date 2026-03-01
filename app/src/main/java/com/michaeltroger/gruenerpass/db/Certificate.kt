package com.michaeltroger.gruenerpass.db

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "certificates")
data class Certificate (
    /**
     * The name of the internally stored pdf file
     */
    @PrimaryKey val id: String,
    /**
     * The user defined und user facing document name
     */
    @ColumnInfo(name = "name") val name: String,
    /**
     * The order in which the document is displayed.
     * Lower values are displayed first.
     */
    @ColumnInfo(name = "displayOrder", defaultValue = "0") val displayOrder: Int = 0,
) : Parcelable
