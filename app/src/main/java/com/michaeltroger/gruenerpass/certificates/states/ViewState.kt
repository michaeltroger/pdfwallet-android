package com.michaeltroger.gruenerpass.certificates.states

import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode

sealed class ViewState {
    abstract val showSwitchLayoutMenuItem: Boolean
    abstract val showLockMenuItem: Boolean
    abstract val showDeleteFilteredMenuItem: Boolean
    abstract val showDeleteAllMenuItem: Boolean
    abstract val showAddMenuItem: Boolean
    abstract val showSettingsMenuItem: Boolean
    abstract val showGetProMenuItem: Boolean
    abstract val showExportFilteredMenuItem: Boolean
    abstract val showExportAllMenuItem: Boolean
    abstract val showAddButton: Boolean
    abstract val showScrollToFirstMenuItem: Boolean
    abstract val showScrollToLastMenuItem: Boolean
    abstract val showSearchMenuItem: Boolean
    abstract val showMoreMenuItem: Boolean
    abstract val showWarningButton: Boolean
    abstract val showChangeOrderMenuItem: Boolean
    abstract val showToggleBarcodeSizeMenuItem: Boolean

    data object Initial : ViewState() {
        override val showSwitchLayoutMenuItem = false
        override val showToggleBarcodeSizeMenuItem = false
        override val showSearchMenuItem = false
        override val showLockMenuItem = false
        override val showDeleteAllMenuItem = false
        override val showDeleteFilteredMenuItem = false
        override val showAddMenuItem = false
        override val showSettingsMenuItem = false
        override val showExportAllMenuItem = false
        override val showExportFilteredMenuItem = false
        override val showAddButton = false
        override val showScrollToFirstMenuItem = false
        override val showScrollToLastMenuItem = false
        override val showMoreMenuItem = true
        override val showWarningButton = false
        override val showChangeOrderMenuItem = false
        override val showGetProMenuItem = false
    }

    data class Empty(
        override val showLockMenuItem: Boolean,
    ) : ViewState() {
        override val showSearchMenuItem = false
        override val showToggleBarcodeSizeMenuItem = false
        override val showSwitchLayoutMenuItem = false
        override val showDeleteFilteredMenuItem = false
        override val showDeleteAllMenuItem = false
        override val showAddMenuItem = false
        override val showSettingsMenuItem = true
        override val showGetProMenuItem = false
        override val showExportAllMenuItem = false
        override val showExportFilteredMenuItem = false
        override val showAddButton = true
        override val showScrollToFirstMenuItem = false
        override val showScrollToLastMenuItem = false
        override val showMoreMenuItem = true
        override val showWarningButton = false
        override val showChangeOrderMenuItem = false
    }

    data class Normal(
        val documents: List<Certificate>,
        val searchBarcode: BarcodeSearchMode,
        val invertColors: Boolean,
        override val showChangeOrderMenuItem: Boolean,
        val filter: String,
        override val showGetProMenuItem: Boolean,
        override val showLockMenuItem: Boolean,
        override val showScrollToFirstMenuItem: Boolean,
        override val showScrollToLastMenuItem: Boolean,
        override val showSearchMenuItem: Boolean,
        override val showWarningButton: Boolean,
        override val showDeleteFilteredMenuItem: Boolean,
        override val showExportFilteredMenuItem: Boolean,
        val showBarcodesInHalfSize: Boolean,
        val generateNewBarcode: Boolean,
    ) : ViewState() {
        override val showSwitchLayoutMenuItem = true
        override val showToggleBarcodeSizeMenuItem = true
        override val showDeleteAllMenuItem = true
        override val showAddMenuItem = true
        override val showSettingsMenuItem = true
        override val showExportAllMenuItem = true
        override val showAddButton = false
        override val showMoreMenuItem = true
    }
}
