package com.michaeltroger.gruenerpass

import androidx.test.core.app.ActivityScenario
import com.michaeltroger.gruenerpass.robots.MainActivityRobot
import com.michaeltroger.gruenerpass.utils.FailingTestWatcher
import org.junit.Rule
import org.junit.Test

class UiTest {

    private val scenario = ActivityScenario.launch(MainActivity::class.java)

    @get:Rule
    val failingTestWatcher = FailingTestWatcher()

    @Test
    fun passwordProtected() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPasswordProtectedPdf(fileName = "password.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyPasswordDialogShown()
            .enterPasswordAndConfirm(password = "test")
            .verifyDocumentLoaded(docName = "password")
    }

    @Test
    fun deleteDocument() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo")
            .clickDeleteDocument()
            .verifyDeleteDialogShown()
            .cancelDelete()
            .verifyDocumentLoaded(docName = "demo")
            .clickDeleteDocument()
            .confirmDelete()
            .verifyEmptyState()
    }

    @Test
    fun shareDocument() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo")
            .clickShareDocument()
            .verifyShareDialogShown()
            .cancelShare()
            .verifyDocumentLoaded(docName = "demo")
    }

    @Test
    fun changeDocumentName() {
        MainActivityRobot()
            .selectFirstDocument()
            .goToPdfFolder(folderName = TestFolders.TEST_GENERIC)
            .openPdf(fileName = "demo.pdf", folderName = TestFolders.TEST_GENERIC)
            .verifyDocumentLoaded(docName = "demo")
            .clickRenameDocument()
            .verifyChangeDocumentNameDialogShown()
            .changeDocumentNameAndConfirm(newDocumentName = "newName")
            .verifyDocumentLoaded(docName = "newName")
    }
}
