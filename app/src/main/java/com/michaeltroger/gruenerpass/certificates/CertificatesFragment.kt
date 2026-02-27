package com.michaeltroger.gruenerpass.certificates

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.michaeltroger.gruenerpass.AddFile
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.barcode.BarcodeRenderer
import com.michaeltroger.gruenerpass.certificates.dialogs.CertificateDialogs
import com.michaeltroger.gruenerpass.certificates.dialogs.CertificateErrors
import com.michaeltroger.gruenerpass.certificates.pager.item.CertificateItem
import com.michaeltroger.gruenerpass.certificates.sharing.PdfSharing
import com.michaeltroger.gruenerpass.certificates.states.TagFilterType
import com.michaeltroger.gruenerpass.certificates.states.ViewEvent
import com.michaeltroger.gruenerpass.certificates.states.ViewState
import com.michaeltroger.gruenerpass.databinding.FragmentCertificatesBinding
import com.michaeltroger.gruenerpass.db.CertificateWithTags
import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Inject

private const val TOUCH_SLOP_FACTOR = 8
private const val SCROLL_TO_DELAY_MS = 500L

@AndroidEntryPoint
class CertificatesFragment : Fragment(R.layout.fragment_certificates) {

    private val vm by viewModels<CertificatesViewModel>()

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val thread = newSingleThreadContext("RenderContext")

    private val adapter = GroupieAdapter()

    private var binding: FragmentCertificatesBinding? = null

    @Inject
    lateinit var pdfSharing: PdfSharing
    @Inject
    lateinit var certificateDialogs: CertificateDialogs
    @Inject
    lateinit var certificateErrors: CertificateErrors
    @Inject
    lateinit var barcodeRenderer: BarcodeRenderer

    private lateinit var menuProvider: CertificatesMenuProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuProvider = CertificatesMenuProvider(requireContext(), vm)
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding = FragmentCertificatesBinding.bind(view)
        val binding = binding!!

        binding.root.setPaddingRelative(
            resources.getDimensionPixelSize(R.dimen.space_small),
            resources.getDimensionPixelSize(R.dimen.space_small),
            0,
            0,
        )

        PagerSnapHelper().attachToRecyclerView(binding.certificates)

        try { // reduce scroll sensitivity for horizontal scrolling to improve vertical scrolling
            val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
            touchSlopField.isAccessible = true
            val touchSlop = touchSlopField.get(binding.certificates) as Int
            touchSlopField.set(binding.certificates, touchSlop * TOUCH_SLOP_FACTOR)
        } catch (ignore: Exception) {}

        binding.certificates.layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false
        )
        binding.certificates.adapter = adapter

        binding.addButton.setOnClickListener {
            vm.onAddFileSelected()
        }

        binding.resetFiltersButton.setOnClickListener {
            vm.onClearFilters()
        }

        binding.filterByTagsButton.setOnClickListener {
            vm.onFilterTagsSelected()
        }

        binding.toggleFilterTypeButton.setOnClickListener {
            vm.onToggleTagFilterType()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.viewState.collect {
                    updateState(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.viewEvent.collect {
                    handleEvent(it)
                }
            }
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun handleEvent(it: ViewEvent) {
        when (it) {
            ViewEvent.CloseAllDialogs -> certificateDialogs.closeAllDialogs()
            ViewEvent.ShowPasswordDialog -> certificateDialogs.showEnterPasswordDialog(
                context = requireContext(),
                onPasswordEntered = vm::onPasswordEntered,
                onCancelled = vm::onPasswordDialogAborted
            )

            ViewEvent.ShowParsingFileError -> {
                binding?.root?.let {
                    certificateErrors.showFileErrorSnackbar(it)
                }
            }
            is ViewEvent.GoToCertificate -> goToCertificate(it)
            ViewEvent.ShowManageTagsDialog -> showManageTagsDialog()
            ViewEvent.ShowFilterTagsDialog -> showFilterTagsDialog()
            is ViewEvent.ShowAssignTagsDialog -> showAssignTagsDialog(it.certificateId)
            is ViewEvent.ShareMultiple -> {
                pdfSharing.openShareAllFilePicker(
                    context = requireContext(),
                    certificates = it.list,
                )
            }
            ViewEvent.ShowDeleteAllDialog -> {
                certificateDialogs.showDoYouWantToDeleteAllDialog(
                    context = requireContext(),
                    onDeleteAllConfirmed = vm::onDeleteAllConfirmed
                )
            }
            is ViewEvent.ShowDeleteFilteredDialog -> {
                certificateDialogs.showDoYouWantToDeleteFilteredDialog(
                    context = requireContext(),
                    onDeleteFilteredConfirmed = vm::onDeleteFilteredConfirmed,
                    documentCount = it.documentCountToBeDeleted
                )
            }
            is ViewEvent.ShowChangeDocumentOrderDialog -> {
                certificateDialogs.showChangeDocumentOrder(
                    context = requireContext(),
                    scope = lifecycleScope,
                    originalOrder = it.originalOrder,
                    onOrderChanged = vm::onOrderChangeConfirmed
                )
            }
            ViewEvent.ShowWarningDialog -> certificateDialogs.showWarningDialog(requireContext())
            ViewEvent.ShowSettingsScreen -> findNavController().navigate(
                CertificatesFragmentDirections.actionGlobalSettingsFragment()
            )
            ViewEvent.ShowGetPro -> findNavController().navigate(
                deepLink = "app://billing".toUri()
            )
            ViewEvent.ShowMoreScreen -> findNavController().navigate(
                CertificatesFragmentDirections.actionGlobalMoreFragment()
            )
            ViewEvent.AddFile -> (requireActivity() as? AddFile)?.addFile()
            is ViewEvent.ShowDeleteDialog -> {
                certificateDialogs.showDoYouWantToDeleteDialog(
                    context = requireContext(),
                    id = it.id,
                    onDeleteConfirmed = vm::onDeleteConfirmed
                )
            }
            is ViewEvent.ShowChangeDocumentNameDialog -> {
                certificateDialogs.showChangeDocumentNameDialog(
                    context = requireContext(),
                    originalDocumentName = it.originalName,
                    onDocumentNameChanged = { newName ->
                        vm.onDocumentNameChangeConfirmed(documentName = newName, filename = it.id)
                    }
                )

            }
            is ViewEvent.Share -> {
                pdfSharing.openShareFilePicker(
                    context = requireContext(),
                    certificate = it.certificate,
                )
            }
        }
    }

    override fun onDestroyView() {
        binding?.certificates?.adapter = null
        binding = null
        super.onDestroyView()
    }

    private fun updateState(state: ViewState) {
        menuProvider.updateMenuState(state)
        binding?.addButton?.isVisible = state.showAddButton

        val normalState = state as? ViewState.Normal

        val isFiltered = normalState?.isFiltered == true
        binding?.filterContainer?.isVisible = isFiltered
        if (isFiltered) {
             val filters = mutableListOf<String>()
             if (normalState.filterSearchText.isNotEmpty()) {
                 filters.add("\"${normalState.filterSearchText}\"")
             }
             filters.addAll(normalState.filterTagNames)
             binding?.filterInfoText?.text = getString(R.string.search_results_format, filters.joinToString(", "))

             binding?.toggleFilterTypeButton?.isVisible = normalState.filterTagIds.isNotEmpty()
             binding?.toggleFilterTypeButton?.text = getString(
                 if (normalState.tagFilterType == TagFilterType.AND) R.string.filter_and else R.string.filter_or
             )
        }

        when (state) {
            is ViewState.Initial -> {} // nothing to do
            is ViewState.Empty -> {
                adapter.clear()
            }
            is ViewState.Normal -> showCertificateState(
                documents = state.documents,
                searchBarcode = state.searchBarcode,
                invertColors = state.invertColors,
                showBarcodesInHalfSize = state.showBarcodesInHalfSize,
                generateNewBarcode = state.generateNewBarcode,
            )
        }
    }

    override fun onPause() {
        super.onPause()
        menuProvider.onPause()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun showCertificateState(
        documents: List<CertificateWithTags>,
        searchBarcode: BarcodeSearchMode,
        invertColors: Boolean,
        showBarcodesInHalfSize: Boolean,
        generateNewBarcode: Boolean,
    ) {
        val items = documents.map { certWithTags ->
            val certificate = certWithTags.certificate
            CertificateItem(
                requireContext().applicationContext,
                fileName = certificate.id,
                barcodeRenderer = barcodeRenderer,
                documentName = certificate.name,
                tags = certWithTags.tags,
                searchBarcode = searchBarcode,
                invertColors = invertColors,
                isDetailView = false,
                dispatcher = thread,
                onDeleteCalled = {
                    vm.onDeleteCalled(certificate.id)
                },
                onDocumentNameClicked = {
                    vm.onChangeDocumentNameSelected(certificate.id, certificate.name)
                },
                onShareCalled = {
                    vm.onShareSelected(certificate)
                },
                onAssignTagsClicked = {
                    vm.onAssignTagsSelected(certificate.id)
                },
                showBarcodesInHalfSize = showBarcodesInHalfSize,
                generateNewBarcode = generateNewBarcode,
            )
        }
        adapter.update(items)
    }

    private fun goToCertificate(event: ViewEvent.GoToCertificate) {
        lifecycleScope.launch {
            if (event.isNewDocument) {
                delay(SCROLL_TO_DELAY_MS)
            }
            binding?.certificates?.smoothScrollToPosition(event.position)
        }
    }

    private fun showFilterTagsDialog() {
        val currentState = vm.viewState.value as? ViewState.Normal ?: return
        val availableTags = currentState.availableTags
        val activeTagIds = currentState.filterTagIds

        certificateDialogs.showFilterTagsDialog(
            context = requireContext(),
            availableTags = availableTags,
            activeTagIds = activeTagIds,
            onTagFilterToggled = vm::onToggleTagFilter,
            onManageTagsClicked = vm::onManageTagsSelected
        )
    }

    private fun showAssignTagsDialog(certificateId: String) {
        val currentState = vm.viewState.value as? ViewState.Normal ?: return
        val availableTags = currentState.availableTags
        val certTags = currentState.documents.find { it.certificate.id == certificateId }?.tags?.map { it.id }?.toSet() ?: emptySet()

        certificateDialogs.showAssignTagsDialog(
            context = requireContext(),
            certificateId = certificateId,
            availableTags = availableTags,
            assignedTagIds = certTags,
            onManageTagsClicked = vm::onManageTagsSelected,
            onTagsAssigned = vm::onUpdateCertificateTags
        )
    }

    private fun showManageTagsDialog() {
        val currentState = vm.viewState.value as? ViewState.Normal ?: return
        val availableTags = currentState.availableTags

        certificateDialogs.showManageTagsDialog(
            context = requireContext(),
            availableTags = availableTags,
            onEditTagClicked = { showEditTagDialog(it) },
            onCreateTagClicked = { showCreateTagDialog() }
        )
    }

    private fun showCreateTagDialog() {
        certificateDialogs.showCreateTagDialog(
            context = requireContext(),
            onTagCreated = vm::onCreateTag,
            onCancel = vm::onManageTagsSelected
        )
    }

    private fun showEditTagDialog(tag: Tag) {
        certificateDialogs.showEditTagDialog(
            context = requireContext(),
            tag = tag,
            onTagRenamed = { id, name ->
                vm.onRenameTag(id, name)
            },
            onDeleteTagClicked = {
                showDeleteTagConfirmationDialog(tag)
            },
            onCancel = vm::onManageTagsSelected
        )
    }

    private fun showDeleteTagConfirmationDialog(tag: Tag) {
        certificateDialogs.showDeleteTagConfirmationDialog(
            context = requireContext(),
            tag = tag,
            onDeleteTagConfirmed = { id ->
                vm.onDeleteTag(id)
            },
            onCancel = { showEditTagDialog(tag) }
        )
    }
}
