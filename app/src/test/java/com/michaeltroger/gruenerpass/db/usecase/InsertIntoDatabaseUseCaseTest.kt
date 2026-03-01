package com.michaeltroger.gruenerpass.db.usecase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.michaeltroger.gruenerpass.db.AppDatabase
import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.db.CertificateTagCrossRef
import com.michaeltroger.gruenerpass.db.Tag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class InsertIntoDatabaseUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: InsertIntoDatabaseUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = InsertIntoDatabaseUseCase(db.certificateDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `inserting in front should preserve tags on existing certificates`() = runTest {
        // Given
        val existingCert = Certificate("id1", "Existing Cert")
        val tag = Tag(1L, "MyTag")
        
        db.certificateDao().insertAll(existingCert)
        db.tagDao().insert(tag)
        db.tagDao().insertCrossRef(CertificateTagCrossRef(existingCert.id, tag.id))

        // Verify setup
        var certWithTags = db.certificateDao().getWithTags(existingCert.id).first()
        assertNotNull(certWithTags)
        assertEquals(1, certWithTags!!.tags.size)
        assertEquals("MyTag", certWithTags.tags[0].name)

        // When
        val newCert = Certificate("id2", "New Cert")
        useCase(newCert, addDocumentInFront = true)

        // Then
        // Check if the old certificate still has the tag
        certWithTags = db.certificateDao().getWithTags(existingCert.id).first()
        assertNotNull("Existing certificate should still exist", certWithTags)
        
        // This assertion is expected to FAIL currently because tags are lost
        assertEquals("Tags should be preserved", 1, certWithTags!!.tags.size)
        assertEquals("MyTag", certWithTags.tags[0].name)
    }
}
