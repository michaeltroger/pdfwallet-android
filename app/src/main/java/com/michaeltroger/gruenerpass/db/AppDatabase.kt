package com.michaeltroger.gruenerpass.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Certificate::class, Tag::class, CertificateTagCrossRef::class], version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ])
abstract class AppDatabase : RoomDatabase() {
    abstract fun certificateDao(): CertificateDao
    abstract fun tagDao(): TagDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE certificates ADD COLUMN displayOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE certificates SET displayOrder = rowid")
            }
        }
    }
}
