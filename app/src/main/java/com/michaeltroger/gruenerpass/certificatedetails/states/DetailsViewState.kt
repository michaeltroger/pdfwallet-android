package com.michaeltroger.gruenerpass.certificatedetails.states

import com.michaeltroger.gruenerpass.db.CertificateWithTags
import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode

sealed class DetailsViewState {
    abstract val showGetProMenuItem: Boolean
    abstract val showToggleBarcodeSizeMenuItem: Boolean
    abstract val showTagMenuItem: Boolean

    data object Initial : DetailsViewState() {
        override val showGetProMenuItem = false
        override val showToggleBarcodeSizeMenuItem = false
        override val showTagMenuItem = false
    }

    data object Deleted : DetailsViewState() {
        override val showGetProMenuItem = false
        override val showToggleBarcodeSizeMenuItem = false
        override val showTagMenuItem = false
    }

    data class Normal(
        val document: CertificateWithTags,
        val availableTags: List<Tag>,
        val searchBarcode: BarcodeSearchMode,
        val invertColors: Boolean,
        val showBarcodesHalfSize: Boolean,
        val generateNewBarcode: Boolean,
        override val showGetProMenuItem: Boolean,
    ) : DetailsViewState() {
        override val showToggleBarcodeSizeMenuItem = true
        override val showTagMenuItem = true
    }
}
