package com.michaeltroger.gruenerpass

import com.michaeltroger.gruenerpass.robots.AndroidFileAppRobot
import com.michaeltroger.gruenerpass.utils.FailingTestWatcher
import org.junit.Rule
import org.junit.Test

class OpenFromFileAppTest {

    @get:Rule
    val failingTestWatcher = FailingTestWatcher()

    @Test
    fun fileOpenedFromFileManager() {
        AndroidFileAppRobot()
            .openFileManagerApp()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo")
    }

    @Test
    fun fileSharedFromFileManager() {
        AndroidFileAppRobot()
            .openFileManagerApp()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .selectPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .selectShare()
            .selectGreenPass()
            .verifyDocumentLoaded(docName = "demo")
    }
}
