package com.michaeltroger.gruenerpass.certificateslist

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michaeltroger.gruenerpass.AddFile
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.certificates.CertificatesMenuProvider
import com.michaeltroger.gruenerpass.certificates.CertificatesViewModel
import com.michaeltroger.gruenerpass.certificates.dialogs.CertificateDialogs
import com.michaeltroger.gruenerpass.certificates.dialogs.CertificateErrors
import com.michaeltroger.gruenerpass.certificates.sharing.PdfSharing
import com.michaeltroger.gruenerpass.certificates.states.TagFilterType
import com.michaeltroger.gruenerpass.certificates.states.ViewEvent
import com.michaeltroger.gruenerpass.certificates.states.ViewState
import com.michaeltroger.gruenerpass.certificateslist.pager.item.CertificateListItem
import com.michaeltroger.gruenerpass.databinding.FragmentCertificatesBinding
import com.michaeltroger.gruenerpass.db.CertificateWithTags
import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CertificatesListFragment : Fragment(R.layout.fragment_certificates) {

    private val vm by viewModels<CertificatesViewModel>()

    private val adapter = GroupieAdapter()

    private var binding: FragmentCertificatesBinding? = null

    @Inject
    lateinit var pdfSharing: PdfSharing
    @Inject
    lateinit var certificateDialogs: CertificateDialogs
    @Inject
    lateinit var certificateErrors: CertificateErrors

    private lateinit var menuProvider: CertificatesMenuProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuProvider = CertificatesMenuProvider(requireContext(), vm, isListLayout = true)
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding = FragmentCertificatesBinding.bind(view)
        val binding = binding!!

        binding.root.setPaddingRelative(
            resources.getDimensionPixelSize(R.dimen.space_small),
            resources.getDimensionPixelSize(R.dimen.space_small),
            resources.getDimensionPixelSize(R.dimen.space_small),
            0,
        )

        binding.certificates.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        binding.certificates.adapter = adapter

        binding.certificates.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

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

        binding.filterHeader.setOnClickListener {
            vm.onToggleFilterExpanded()
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
                CertificatesListFragmentDirections.actionGlobalSettingsFragment()
            )
            ViewEvent.ShowGetPro -> findNavController().navigate(
                deepLink = "app://billing".toUri()
            )
            ViewEvent.ShowMoreScreen -> findNavController().navigate(
                CertificatesListFragmentDirections.actionGlobalMoreFragment()
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

        updateSearchResults(state)

        when (state) {
            is ViewState.Initial -> {} // nothing to do
            is ViewState.Empty -> {
                adapter.clear()
            }
            is ViewState.Normal -> showCertificateState(
                documents = state.documents,
                searchBarcode = state.searchBarcode != BarcodeSearchMode.DISABLED,
            )
        }
    }

    private fun updateSearchResults(normalState: ViewState) {
        if (normalState !is ViewState.Normal) return
        binding?.filterContainer?.isVisible = normalState.isFiltered
        val queryText = "\"${normalState.filterSearchText}\""
        val tagsText = "\"${normalState.filterTagNames.joinToString()}\""
        if (normalState.isFiltered) {
            binding?.filterInfoText?.text = when {
                normalState.filterTagNames.isNotEmpty() && normalState.filterSearchText.isEmpty()
                    -> getString(R.string.search_results_overview_tags, tagsText)

                normalState.filterTagNames.isEmpty() && normalState.filterSearchText.isNotEmpty()
                    -> getString(R.string.search_results_overview_query, queryText)

                normalState.filterTagNames.isNotEmpty() && normalState.filterSearchText.isNotEmpty()
                    -> getString(
                    R.string.search_results_overview_query_and_tags,
                    queryText,
                    tagsText
                )

                else -> ""
            }

            binding?.filterTagModeWrapper?.isVisible = normalState.filterTagNames.isNotEmpty()
            binding?.toggleFilterTypeButton?.text = getString(
                if (normalState.tagFilterType == TagFilterType.AND) R.string.filter_and else R.string.filter_or
            )
            binding?.filterControls?.isVisible = normalState.isFilterExpanded
            binding?.filterExpandIcon?.rotation = if (normalState.isFilterExpanded) 180f else 0f
        }
    }

    override fun onPause() {
        super.onPause()
        menuProvider.onPause()
    }

    private fun showCertificateState(documents: List<CertificateWithTags>, searchBarcode: Boolean) {
        val items = documents.map { certWithTags ->
            val certificate = certWithTags.certificate
            CertificateListItem(
                fileName = certificate.id,
                documentName = certificate.name,
                searchBarcode = searchBarcode,
                tags = certWithTags.tags,
                onDeleteCalled = {
                    vm.onDeleteCalled(certificate.id)
                },
                onChangeDocumentNameClicked = {
                    vm.onChangeDocumentNameSelected(certificate.id, certificate.name)
                },
                onOpenDetails = {
                    findNavController().navigate(
                        CertificatesListFragmentDirections.navigateToCertificateDetails(certificate.id)
                    )
                },
                onShareCalled = {
                    vm.onShareSelected(certificate)
                },
                onAssignTagsClicked = {
                    vm.onAssignTagsSelected(certificate.id)
                },
            )
        }
        adapter.update(items)
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

    private fun goToCertificate(event: ViewEvent.GoToCertificate) {
        lifecycleScope.launch {
            withStarted {
                binding?.certificates?.scrollToPosition(event.position)
                findNavController().navigate(
                    CertificatesListFragmentDirections.navigateToCertificateDetails(event.id)
                )
            }
        }
    }
}
