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
import org.junit.Ignore
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

        ScreenshotUtil.recordScreenshot("qr_code_crop")
    }

    @Test
    fun aztecCode() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "aztec.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "aztec", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("aztec_code_crop")
    }

    @Test
    fun dataMatrixCode() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "datamatrix.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "datamatrix", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("data_matrix_crop")
    }

    @Test
    fun dataMatrixErezept() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "datamatrix_erezept.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "datamatrix_erezept", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("data_matrix_erezept_crop")
    }

    @Test
    fun pdf417Code() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "pdf417.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "pdf417", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("pdf417_crop")
    }

    @Test
    fun qrSpecial() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "qr_logo.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "qr_logo", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("qr_logo_crop")
    }

    @Ignore
    @Test
    fun codabar() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "codabar.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "codabar", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("codabar_crop")
    }

    @Ignore
    @Test
    fun code39() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "code39.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "code39", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("code39_crop")
    }

    @Ignore
    @Test
    fun code93() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "code93.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "code93", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("code93_crop")
    }

    @Ignore
    @Test
    fun code128() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "code128.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "code128", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("code128_crop")
    }

    @Ignore
    @Test
    fun ean8() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "ean8.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "ean8", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("ean8_crop")
    }

    @Ignore
    @Test
    fun ean13() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "ean13.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "ean13", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("ean13_crop")
    }

    @Ignore
    @Test
    fun itf() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "itf.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "itf", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("itf_crop")
    }

    @Ignore
    @Test
    fun usbca() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "usbca.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "usbca", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("usbca_crop")
    }

    @Ignore
    @Test
    fun usbce() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "usbce.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "usbce", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("usbce_crop")
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
