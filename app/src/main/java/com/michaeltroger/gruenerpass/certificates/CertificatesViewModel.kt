package com.michaeltroger.gruenerpass.certificates

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.certificates.mapper.toCertificate
import com.michaeltroger.gruenerpass.certificates.states.TagFilterType
import com.michaeltroger.gruenerpass.certificates.states.ViewEvent
import com.michaeltroger.gruenerpass.certificates.states.ViewState
import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.db.CertificateWithTags
import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.db.usecase.ChangeCertificateNameUseCase
import com.michaeltroger.gruenerpass.db.usecase.ChangeCertificateOrderUseCase
import com.michaeltroger.gruenerpass.db.usecase.CreateTagUseCase
import com.michaeltroger.gruenerpass.db.usecase.DeleteAllCertificatesUseCase
import com.michaeltroger.gruenerpass.db.usecase.DeleteSelectedCertificatesUseCase
import com.michaeltroger.gruenerpass.db.usecase.DeleteSingleCertificateUseCase
import com.michaeltroger.gruenerpass.db.usecase.DeleteTagUseCase
import com.michaeltroger.gruenerpass.db.usecase.GetCertificatesFlowUseCase
import com.michaeltroger.gruenerpass.db.usecase.GetTagsUseCase
import com.michaeltroger.gruenerpass.db.usecase.InsertIntoDatabaseUseCase
import com.michaeltroger.gruenerpass.db.usecase.RenameTagUseCase
import com.michaeltroger.gruenerpass.db.usecase.UpdateCertificateTagsUseCase
import com.michaeltroger.gruenerpass.lock.AppLockedRepo
import com.michaeltroger.gruenerpass.pdfimporter.PdfImportResult
import com.michaeltroger.gruenerpass.pdfimporter.PdfImporter
import com.michaeltroger.gruenerpass.pro.IsProUnlockedUseCase
import com.michaeltroger.gruenerpass.pro.PurchaseUpdateUseCase
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode
import com.michaeltroger.gruenerpass.settings.getBooleanFlow
import com.michaeltroger.gruenerpass.settings.getFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions", "LongParameterList")
@HiltViewModel
class CertificatesViewModel @Inject constructor(
    app: Application,
    private val pdfImporter: PdfImporter,
    private val insertIntoDatabaseUseCase: InsertIntoDatabaseUseCase,
    private val deleteAllCertificatesUseCase: DeleteAllCertificatesUseCase,
    private val deleteSingleCertificateUseCase: DeleteSingleCertificateUseCase,
    private val deleteSelectedCertificatesUseCase: DeleteSelectedCertificatesUseCase,
    private val changeCertificateNameUseCase: ChangeCertificateNameUseCase,
    private val changeCertificateOrderUseCase: ChangeCertificateOrderUseCase,
    private val getCertificatesFlowUseCase: GetCertificatesFlowUseCase,
    private val lockedRepo: AppLockedRepo,
    private val sharedPrefs: SharedPreferences,
    private val isProUnlocked: IsProUnlockedUseCase,
    private val purchaseUpdateUseCase: PurchaseUpdateUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val renameTagUseCase: RenameTagUseCase,
    private val updateCertificateTagsUseCase: UpdateCertificateTagsUseCase,
): AndroidViewModel(app) {

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(
        ViewState.Initial
    )
    val viewState: StateFlow<ViewState> = _viewState

    private val filterSearchText = MutableStateFlow("")
    private val filterTags = MutableStateFlow<Set<Long>>(emptySet())
    private val filterTagType = MutableStateFlow(TagFilterType.AND)

    private val _viewEvent = MutableSharedFlow<ViewEvent>(extraBufferCapacity = 1)
    val viewEvent: SharedFlow<ViewEvent> = _viewEvent

    private val shouldAuthenticate =
        sharedPrefs.getBooleanFlow(
            app.getString(R.string.key_preference_biometric),
            false
        )
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
    private val addDocumentsInFront =
        sharedPrefs.getBooleanFlow(
            app.getString(R.string.key_preference_add_documents_front),
            false
        )
    private val showOnLockedScreen =
        sharedPrefs.getBooleanFlow(
            app.getString(R.string.key_preference_show_on_locked_screen),
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
                getCertificatesFlowUseCase(),
                filterSearchText,
                shouldAuthenticate,
                searchForBarcode,
                invertColors,
                showOnLockedScreen,
                showBarcodesHalfSize,
                generateNewBarcode,
                purchaseUpdateUseCase(),
                getTagsUseCase(),
                filterTags,
                filterTagType,
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                updateState(
                    docs = values[0] as List<CertificateWithTags>,
                    filterSearchText = values[1] as String,
                    shouldAuthenticate = values[2] as Boolean,
                    searchForBarcode = values[3] as BarcodeSearchMode,
                    invertColors = values[4] as Boolean,
                    showOnLockedScreen = values[5] as Boolean,
                    showBarcodesInHalfSize = values[6] as Boolean,
                    generateNewBarcode = values[7] as Boolean,
                    availableTags = values[9] as List<Tag>,
                    filterTagIds = values[10] as Set<Long>,
                    tagFilterType = values[11] as TagFilterType,
                )
            }.collect()
        }
        viewModelScope.launch {
            pdfImporter.hasPendingFile().filter { it }.collect {
                _viewEvent.emit(ViewEvent.CloseAllDialogs)
                processPendingFile()
            }
        }
    }

    @Suppress("MagicNumber")
    private suspend fun updateState(
        docs: List<CertificateWithTags>,
        filterSearchText: String,
        shouldAuthenticate: Boolean,
        searchForBarcode: BarcodeSearchMode,
        invertColors: Boolean,
        showOnLockedScreen: Boolean,
        showBarcodesInHalfSize: Boolean,
        generateNewBarcode: Boolean,
        availableTags: List<Tag>,
        filterTagIds: Set<Long>,
        tagFilterType: TagFilterType,
    ) {
        if (docs.isEmpty()) {
            _viewState.emit(
                ViewState.Empty(
                    showLockMenuItem = shouldAuthenticate,
                )
            )
        } else {
            val filteredDocs = docs.filter { certWithTags ->
                val matchesNameOrTag = if (filterSearchText.isEmpty()) {
                    true
                } else {
                    certWithTags.certificate.name.contains(filterSearchText, ignoreCase = true) ||
                        certWithTags.tags.any { it.name.contains(filterSearchText, ignoreCase = true) }
                }
                val matchesTags = if (filterTagIds.isEmpty()) {
                    true
                } else {
                    if (tagFilterType == TagFilterType.AND) {
                        filterTagIds.all { tagId ->
                            certWithTags.tags.any { it.id == tagId }
                        }
                    } else {
                        filterTagIds.any { tagId ->
                            certWithTags.tags.any { it.id == tagId }
                        }
                    }
                }
                matchesNameOrTag && matchesTags
            }
            val areDocumentsFilteredOut = filteredDocs.size != docs.size
            val filterTagNames = availableTags.filter { it.id in filterTagIds }.map { it.name }
            _viewState.emit(
                ViewState.Normal(
                    documents = filteredDocs,
                    searchBarcode = searchForBarcode,
                    invertColors = invertColors,
                    availableTags = availableTags,
                    filterTagIds = filterTagIds,
                    showLockMenuItem = shouldAuthenticate,
                    showScrollToFirstMenuItem = filteredDocs.size > 1,
                    showScrollToLastMenuItem = filteredDocs.size > 1,
                    showChangeOrderMenuItem = !areDocumentsFilteredOut && filterSearchText.isEmpty() && filterTagIds.isEmpty() && docs.size > 1,
                    showSearchMenuItem = docs.size > 1,
                    filterSearchText = filterSearchText,
                    filterTagNames = filterTagNames,
                    showWarningButton = showOnLockedScreen,
                    showExportFilteredMenuItem = areDocumentsFilteredOut && filteredDocs.isNotEmpty(),
                    showDeleteFilteredMenuItem = areDocumentsFilteredOut && filteredDocs.isNotEmpty(),
                    showGetProMenuItem = !isProUnlocked(),
                    showBarcodesInHalfSize = showBarcodesInHalfSize,
                    generateNewBarcode = generateNewBarcode,
                    isFiltered = filterSearchText.isNotEmpty() || filterTagIds.isNotEmpty(),
                    tagFilterType = tagFilterType,
                )
            )
        }
    }

    fun onClearFilters() {
        filterSearchText.value = ""
        filterTags.value = emptySet()
        filterTagType.value = TagFilterType.AND
    }

    private suspend fun processPendingFile(password: String? = null) {
        when (val result = pdfImporter.importPendingFile(password = password)) {
            PdfImportResult.ParsingError -> {
                _viewEvent.emit(ViewEvent.ShowParsingFileError)
            }
            is PdfImportResult.PasswordRequired -> {
                _viewEvent.emit(ViewEvent.ShowPasswordDialog)
            }
            is PdfImportResult.Success -> {
                insertIntoDatabase(result.pendingCertificate.toCertificate())
            }
            PdfImportResult.NoFileToImport -> {
                // ignore
            }
        }
    }

    fun onPasswordEntered(password: String) = viewModelScope.launch {
        processPendingFile(password = password)
    }

    private suspend fun insertIntoDatabase(certificate: Certificate) {
        val addDocumentsInFront = addDocumentsInFront.first()
        insertIntoDatabaseUseCase(certificate, addDocumentsInFront)
        val event = if (addDocumentsInFront) {
            ViewEvent.GoToCertificate(
                position = 0,
                id = certificate.id,
                isNewDocument = true,
            )
        } else {
            ViewEvent.GoToCertificate(
                position = getCertificatesFlowUseCase().first().size - 1,
                id = certificate.id,
                isNewDocument = true,
            )
        }
        _viewEvent.emit(event)
    }

    fun onDocumentNameChangeConfirmed(filename: String, documentName: String) = viewModelScope.launch {
         changeCertificateNameUseCase(filename, documentName)
    }

    fun onDeleteConfirmed(fileName: String) = viewModelScope.launch {
        deleteSingleCertificateUseCase(fileName)
    }

    fun onDeleteAllConfirmed() = viewModelScope.launch {
        deleteAllCertificatesUseCase()
    }

    fun onDeleteFilteredConfirmed() = viewModelScope.launch {
        val docs = (viewState.value as? ViewState.Normal)?.documents ?: return@launch
        deleteSelectedCertificatesUseCase(docs.map { it.certificate })
    }

    @Suppress("SpreadOperator")
    fun onOrderChangeConfirmed(sortedIdList: List<String>) = viewModelScope.launch {
        changeCertificateOrderUseCase(sortedIdList)
    }

    fun lockApp() = viewModelScope.launch {
        lockedRepo.lockApp()
    }

    fun onPasswordDialogAborted() {
        pdfImporter.deletePendingFile()
    }

    fun onSearchQueryChanged(query: String) = viewModelScope.launch {
        filterSearchText.value = query.trim()
    }

    fun onExportFilteredSelected() = viewModelScope.launch {
        val docs = (viewState.value as? ViewState.Normal)?.documents ?: return@launch
        _viewEvent.emit(
            ViewEvent.ShareMultiple(docs.map { it.certificate })
        )
    }

    fun onExportAllSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShareMultiple(getCertificatesFlowUseCase().first().map { it.certificate })
        )
    }

    fun onDeleteFilteredSelected() = viewModelScope.launch {
        val docsSize = (viewState.value as? ViewState.Normal)?.documents?.size ?: return@launch
        _viewEvent.emit(
            ViewEvent.ShowDeleteFilteredDialog(documentCountToBeDeleted = docsSize)
        )
    }

    fun onDeleteAllSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowDeleteAllDialog
        )
    }

    fun onScrollToFirstSelected() = viewModelScope.launch {
        val docs = (viewState.value as? ViewState.Normal)?.documents ?: return@launch
        _viewEvent.emit(
            ViewEvent.GoToCertificate(
                position = 0,
                id = docs[0].certificate.id,
                isNewDocument = false,
            )
        )
    }

    fun onScrollToLastSelected() = viewModelScope.launch {
        val docs = (viewState.value as? ViewState.Normal)?.documents ?: return@launch
        val indexLast = docs.size - 1
        _viewEvent.emit(
            ViewEvent.GoToCertificate(
                position = indexLast,
                id = docs[indexLast].certificate.id,
                isNewDocument = false,
            )
        )
    }

    fun onChangeOrderSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowChangeDocumentOrderDialog(
                originalOrder = getCertificatesFlowUseCase().first().map { it.certificate }
            )
        )
    }

    fun onCreateTag(name: String) = viewModelScope.launch {
        createTagUseCase(name)
        delay(500)
        _viewEvent.emit(ViewEvent.ShowManageTagsDialog)
    }

    fun onRenameTag(id: Long, newName: String) = viewModelScope.launch {
        renameTagUseCase(id, newName)
        delay(500)
        _viewEvent.emit(ViewEvent.ShowManageTagsDialog)
    }

    fun onDeleteTag(id: Long) = viewModelScope.launch {
        deleteTagUseCase(id)
        val currentFilter = filterTags.value
        if (id in currentFilter) {
            filterTags.value = currentFilter - id
        }
        delay(500)
        _viewEvent.emit(ViewEvent.ShowManageTagsDialog)
    }

    fun onToggleTagFilter(id: Long) = viewModelScope.launch {
        val currentFilter = filterTags.value
        if (id in currentFilter) {
            filterTags.value = currentFilter - id
        } else {
            filterTags.value = currentFilter + id
        }
    }

    fun onToggleTagFilterType() = viewModelScope.launch {
        val current = filterTagType.value
        filterTagType.value = if (current == TagFilterType.AND) TagFilterType.OR else TagFilterType.AND
    }
    
    fun onUpdateCertificateTags(certificateId: String, tagIds: List<Long>) = viewModelScope.launch {
        updateCertificateTagsUseCase(certificateId, tagIds)
    }

    fun onFilterTagsSelected() = viewModelScope.launch {
        _viewEvent.emit(ViewEvent.ShowFilterTagsDialog)
    }

    fun onAssignTagsSelected(certificateId: String) = viewModelScope.launch {
        _viewEvent.emit(ViewEvent.ShowAssignTagsDialog(certificateId))
    }

    fun onManageTagsSelected() = viewModelScope.launch {
        _viewEvent.emit(ViewEvent.ShowManageTagsDialog)
    }

    fun onShowWarningDialogSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowWarningDialog
        )
    }

    fun onGetPro() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowGetPro
        )
    }

    fun onShowSettingsSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowSettingsScreen
        )
    }

    fun onShowMoreSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.ShowMoreScreen
        )
    }

    fun onAddFileSelected() = viewModelScope.launch {
        _viewEvent.emit(
            ViewEvent.AddFile
        )
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

    fun onSwitchLayoutSelected() = viewModelScope.launch {
        sharedPrefs.edit {
            putBoolean(
                getApplication<Application>().getString(R.string.key_preference_show_list_layout),
                !(sharedPrefs.getBoolean(
                    getApplication<Application>().getString(R.string.key_preference_show_list_layout),
                    false
                ))
            )
        }
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
