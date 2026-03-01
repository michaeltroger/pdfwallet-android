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
class ChangeCertificateOrderUseCaseTest {

    private lateinit var db: AppDatabase
    private lateinit var useCase: ChangeCertificateOrderUseCase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        useCase = ChangeCertificateOrderUseCase(db.certificateDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `changing order should update displayOrder and preserve tags`() = runTest {
        // Given
        val cert1 = Certificate("id1", "Cert 1", displayOrder = 0)
        val cert2 = Certificate("id2", "Cert 2", displayOrder = 1)
        val cert3 = Certificate("id3", "Cert 3", displayOrder = 2)
        val tag = Tag(1L, "MyTag")
        
        db.certificateDao().insertAll(cert1, cert2, cert3)
        db.tagDao().insert(tag)
        db.tagDao().insertCrossRef(CertificateTagCrossRef(cert1.id, tag.id))

        // Verify initial state
        var cert1WithTags = db.certificateDao().getWithTags(cert1.id).first()
        assertNotNull(cert1WithTags)
        assertEquals(1, cert1WithTags!!.tags.size)

        // When
        // Reverse order: 3, 2, 1
        val newOrder = listOf("id3", "id2", "id1")
        useCase(newOrder)

        // Then
        // Verify orders
        val c3 = db.certificateDao().get("id3").first()
        val c2 = db.certificateDao().get("id2").first()
        val c1 = db.certificateDao().get("id1").first()

        assertEquals(0, c3!!.displayOrder)
        assertEquals(1, c2!!.displayOrder)
        assertEquals(2, c1!!.displayOrder)

        // Verify tags preserved on cert1
        cert1WithTags = db.certificateDao().getWithTags(cert1.id).first()
        assertNotNull(cert1WithTags)
        assertEquals(1, cert1WithTags!!.tags.size)
        assertEquals("MyTag", cert1WithTags.tags[0].name)
    }
}
