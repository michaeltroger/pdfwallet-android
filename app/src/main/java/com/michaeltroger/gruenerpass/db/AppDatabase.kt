package com.michaeltroger.gruenerpass.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Certificate::class, Tag::class, CertificateTagCrossRef::class], version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ])
abstract class AppDatabase : RoomDatabase() {
    abstract fun certificateDao(): CertificateDao
    abstract fun tagDao(): TagDao
}
