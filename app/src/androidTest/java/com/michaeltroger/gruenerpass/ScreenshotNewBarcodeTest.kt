package com.michaeltroger.gruenerpass

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

class ScreenshotNewBarcodeTest {

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
    fun qrSpecial() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_2D)
            .openPdf(fileName = "qr_logo.pdf", folderName = TestFolders.TEST_2D)
            .verifyDocumentLoaded(docName = "qr_logo", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("qr_logo")
    }

    @Ignore
    @Test
    fun codabar() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "codabar.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "codabar", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("codabar")
    }

    @Ignore
    @Test
    fun code39() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "code39.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "code39", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("code39")
    }

    @Ignore
    @Test
    fun code93() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "code93.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "code93", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("code93")
    }

    @Ignore
    @Test
    fun code128() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "code128.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "code128", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("code128")
    }

    @Ignore
    @Test
    fun ean8() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "ean8.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "ean8", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("ean8")
    }

    @Ignore
    @Test
    fun ean13() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "ean13.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "ean13", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("ean13")
    }

    @Ignore
    @Test
    fun itf() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "itf.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "itf", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("itf")
    }

    @Ignore
    @Test
    fun usbca() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "usbca.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "usbca", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("usbca")
    }

    @Ignore
    @Test
    fun usbce() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_1D)
            .openPdf(fileName = "usbce.pdf", folderName = TestFolders.TEST_1D)
            .verifyDocumentLoaded(docName = "usbce", expectBarcode = true)

        ScreenshotUtil.recordScreenshot("usbce")
    }
}
