package com.michaeltroger.gruenerpass

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.michaeltroger.gruenerpass.robots.MainActivityRobot
import com.michaeltroger.gruenerpass.utils.FailingTestWatcher
import com.michaeltroger.gruenerpass.utils.ScreenshotUtil
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ScreenshotTest {

    private lateinit var scenario: ActivityScenario<MainActivity>

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @get:Rule
    val failingTestWatcher = FailingTestWatcher()

    @Before
    fun startUp() {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putBoolean(context.getString(R.string.key_preference_prevent_screenshots), false)
            putBoolean(context.getString(R.string.key_preference_new_barcode_generation), true)
        }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun emptyState() {
        MainActivityRobot().verifyEmptyState()
        ScreenshotUtil.recordScreenshot("empty_state")
    }

    @Test
    fun normalState() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo")

        ScreenshotUtil.recordScreenshot("normal_state")
    }

    @Test
    fun multipleDocuments() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo", expectedDocumentCount = 1)
            .selectAnotherDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo1.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo1", expectedDocumentCount = 2)

        ScreenshotUtil.recordScreenshot("multiple_documents")
    }

    @Test
    fun qrCode() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "qr.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "qr", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("qr_code")
    }

    @Test
    fun aztecCode() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "aztec.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "aztec", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("aztec_code")
    }

    @Test
    fun dataMatrixCode() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "datamatrix.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "datamatrix", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("data_matrix")
    }

    @Test
    fun dataMatrixErezept() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "datamatrix_erezept.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "datamatrix_erezept", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("data_matrix_erezept")
    }

    @Test
    fun pdf417Code() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "pdf417.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "pdf417", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("pdf417")
    }

    @Test
    fun darkMode() {
        enableDarkMode()
        MainActivityRobot().verifyEmptyState()

        ScreenshotUtil.recordScreenshot("dark_mode")
    }

    private fun enableDarkMode() {
        scenario.onActivity {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }
}
