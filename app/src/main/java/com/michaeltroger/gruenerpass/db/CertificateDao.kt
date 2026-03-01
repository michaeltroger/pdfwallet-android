package com.michaeltroger.gruenerpass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates ORDER BY displayOrder ASC")
    fun getAll(): Flow<List<Certificate>>

    @Transaction
    @Query("SELECT * FROM certificates ORDER BY displayOrder ASC")
    fun getAllWithTags(): Flow<List<CertificateWithTags>>

    @Query("SELECT * FROM certificates WHERE id = :id")
    fun get(id: String): Flow<Certificate?>

    @Transaction
    @Query("SELECT * FROM certificates WHERE id = :id")
    fun getWithTags(id: String): Flow<CertificateWithTags?>

    @Insert
    suspend fun insertAll(vararg certificates: Certificate)

    @Query("UPDATE certificates SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String): Int

    @Query("UPDATE certificates SET displayOrder = :order WHERE id = :id")
    suspend fun updateOrder(id: String, order: Int)

    @Query("SELECT MIN(displayOrder) FROM certificates")
    suspend fun getMinOrder(): Int?

    @Query("SELECT MAX(displayOrder) FROM certificates")
    suspend fun getMaxOrder(): Int?

    @Query("UPDATE certificates SET displayOrder = displayOrder + :shift")
    suspend fun shiftAllOrders(shift: Int)

    @Query("DELETE FROM certificates WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM certificates")
    suspend fun deleteAll()
}
