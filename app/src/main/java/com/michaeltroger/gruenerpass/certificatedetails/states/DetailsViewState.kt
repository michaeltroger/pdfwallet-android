package com.michaeltroger.gruenerpass.certificatedetails.states

import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode

sealed class DetailsViewState {
    abstract val showGetProMenuItem: Boolean
    abstract val showToggleBarcodeSizeMenuItem: Boolean

    data object Initial : DetailsViewState() {
        override val showGetProMenuItem = false
        override val showToggleBarcodeSizeMenuItem = false
    }

    data object Deleted : DetailsViewState() {
        override val showGetProMenuItem = false
        override val showToggleBarcodeSizeMenuItem = false
    }

    data class Normal(
        val document: Certificate,
        val searchBarcode: BarcodeSearchMode,
        val invertColors: Boolean,
        val showBarcodesHalfSize: Boolean,
        val generateNewBarcode: Boolean,
        override val showGetProMenuItem: Boolean,
    ) : DetailsViewState() {
        override val showToggleBarcodeSizeMenuItem = true
    }
}
