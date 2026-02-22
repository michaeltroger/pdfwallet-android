package com.michaeltroger.gruenerpass.certificatedetails

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.certificatedetails.states.DetailsViewState
import com.michaeltroger.gruenerpass.certificates.states.ViewEvent
import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.db.usecase.ChangeCertificateNameUseCase
import com.michaeltroger.gruenerpass.db.usecase.DeleteSingleCertificateUseCase
import com.michaeltroger.gruenerpass.db.usecase.GetSingleCertificateFlowUseCase
import com.michaeltroger.gruenerpass.pro.IsProUnlockedUseCase
import com.michaeltroger.gruenerpass.pro.PurchaseUpdateUseCase
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode
import com.michaeltroger.gruenerpass.settings.getBooleanFlow
import com.michaeltroger.gruenerpass.settings.getFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CertificateDetailsViewModel @Inject constructor(
    app: Application,
    private val deleteSingleCertificateUseCase: DeleteSingleCertificateUseCase,
    private val changeCertificateNameUseCase: ChangeCertificateNameUseCase,
    private val getSingleCertificateFlowUseCase: GetSingleCertificateFlowUseCase,
    private val isProUnlocked: IsProUnlockedUseCase,
    private val sharedPrefs: SharedPreferences,
    private val purchaseUpdateUseCase: PurchaseUpdateUseCase,
    savedStateHandle: SavedStateHandle,
): AndroidViewModel(app) {

    private val _viewState: MutableStateFlow<DetailsViewState> = MutableStateFlow(
        DetailsViewState.Initial
    )
    val viewState: StateFlow<DetailsViewState> = _viewState

    private val id: String = CertificateDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle).id

    private val _viewEvent = MutableSharedFlow<ViewEvent>(extraBufferCapacity = 1)
    val viewEvent: SharedFlow<ViewEvent> = _viewEvent

    private val searchForBarcode =
        sharedPrefs.getFlow(
            app.getString(R.string.key_preference_extract_barcodes),
            app.getString(R.string.key_preference_barcodes_extended)
        ) { value: String ->
            BarcodeSearchMode.fromPrefValue(value)
        }

    private val invertColors =
        sharedPrefs.getBooleanFlow(
            app.getString(R.string.key_preference_invert_pdf_colors),
            false
        )

    private val showBarcodesHalfSize =
        sharedPrefs.getBooleanFlow(
            app.getString(R.string.key_preference_half_size_barcodes),
            false
        )

    private val generateNewBarcode =
        sharedPrefs.getBooleanFlow(
            app.getString(R.string.key_preference_new_barcode_generation),
            false
        )

    init {
        viewModelScope.launch {
            combine(
                getSingleCertificateFlowUseCase(id),
                searchForBarcode,
                invertColors,
                showBarcodesHalfSize,
                generateNewBarcode,
                purchaseUpdateUseCase(),
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                updateState(
                    document = values[0] as? Certificate,
                    searchForBarcode = values[1] as BarcodeSearchMode,
                    invertColors = values[2] as Boolean,
                    showBarcodesHalfSize = values[3] as Boolean,
                    generateNewBarcode = values[4] as Boolean
                )
            }.collect()
        }
    }

    @Suppress("UnusedParameter")
    private suspend fun updateState(
        document: Certificate?,
        searchForBarcode: BarcodeSearchMode,
        invertColors: Boolean,
        showBarcodesHalfSize: Boolean,
        generateNewBarcode: Boolean,
    ) {
        if (document == null) {
            _viewState.emit(DetailsViewState.Deleted)
        } else {
            _viewState.emit(
                DetailsViewState.Normal(
                    document = document,
                    searchBarcode = searchForBarcode,
                    invertColors = invertColors,
                    showBarcodesHalfSize = showBarcodesHalfSize,
                    showGetProMenuItem = !isProUnlocked(),
                    generateNewBarcode = generateNewBarcode
                )
            )
        }
    }

    fun onDocumentNameChangeConfirmed(filename: String, documentName: String) = viewModelScope.launch {
         changeCertificateNameUseCase(filename, documentName)
    }

    fun onDeleteConfirmed(fileName: String) = viewModelScope.launch {
        deleteSingleCertificateUseCase(fileName)
    }

    fun onDeleteCalled(id: String) = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowDeleteDialog(
                id = id,
            )
        )
    }

    fun onChangeDocumentNameSelected(id: String, name: String) = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowChangeDocumentNameDialog(
                id = id,
                originalName = name
            )
        )
    }

    fun onShareSelected(certificate: Certificate) = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.Share(certificate)
        )
    }

    fun onGetPro() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowGetPro
        )
    }

    fun toggleBarcodeSize() = viewModelScope.launch {
        sharedPrefs.edit {
            val isCurrentlyEnabled = (sharedPrefs.getBoolean(
                getApplication<Application>().getString(R.string.key_preference_half_size_barcodes),
                false
            ))
            if (isProUnlocked()) {
                putBoolean(
                    getApplication<Application>().getString(R.string.key_preference_half_size_barcodes),
                    !isCurrentlyEnabled
                )
            } else if (isCurrentlyEnabled) {
                putBoolean(
                    getApplication<Application>().getString(R.string.key_preference_half_size_barcodes),
                    false
                )
            } else {
                _viewEvent.emit(
                    ViewEvent.ShowGetPro
                )
            }
        }
    }
}
