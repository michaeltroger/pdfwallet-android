package com.michaeltroger.gruenerpass.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun get(id: Long): Tag?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)
    
    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: CertificateTagCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: CertificateTagCrossRef)

    @Query("DELETE FROM CertificateTagCrossRef WHERE certificateId = :certificateId AND tagId = :tagId")
    suspend fun deleteCrossRef(certificateId: String, tagId: Long)
    
    @Query("DELETE FROM CertificateTagCrossRef WHERE certificateId = :certificateId")
    suspend fun deleteCrossRefsForCertificate(certificateId: String)
}
