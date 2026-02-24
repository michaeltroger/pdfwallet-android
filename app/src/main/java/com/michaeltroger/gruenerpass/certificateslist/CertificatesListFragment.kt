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
import com.michaeltroger.gruenerpass.certificates.states.ViewEvent
import com.michaeltroger.gruenerpass.certificates.states.ViewState
import com.michaeltroger.gruenerpass.certificateslist.pager.item.CertificateListItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaeltroger.gruenerpass.databinding.FragmentCertificatesBinding
import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.db.CertificateWithTags
import com.michaeltroger.gruenerpass.db.Tag
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode
import android.widget.EditText
import android.widget.FrameLayout
import android.view.LayoutInflater
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
        val activeTagIds = currentState.activeTagIds

        val tagNames = availableTags.map { it.name }.toTypedArray()
        val checkedItems = availableTags.map { it.id in activeTagIds }.toBooleanArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.filter_by_tags)
            .setMultiChoiceItems(tagNames, checkedItems) { _, which, isChecked ->
                vm.onToggleTagFilter(availableTags[which].id)
            }
            .setNeutralButton(R.string.manage_tags) { _, _ ->
                vm.onManageTagsSelected()
            }
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showAssignTagsDialog(certificateId: String) {
        val currentState = vm.viewState.value as? ViewState.Normal ?: return
        val availableTags = currentState.availableTags
        val certTags = currentState.documents.find { it.certificate.id == certificateId }?.tags?.map { it.id }?.toSet() ?: emptySet()
        
        val tagNames = availableTags.map { it.name }.toTypedArray()
        val checkedItems = availableTags.map { it.id in certTags }.toBooleanArray()
        val selectedTagIds = certTags.toMutableSet()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.assign_tags)
            .setMultiChoiceItems(tagNames, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedTagIds.add(availableTags[which].id)
                } else {
                    selectedTagIds.remove(availableTags[which].id)
                }
            }
            .setNeutralButton(R.string.manage_tags) { _, _ ->
                vm.onManageTagsSelected()
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                vm.onUpdateCertificateTags(certificateId, selectedTagIds.toList())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showManageTagsDialog() {
        val currentState = vm.viewState.value as? ViewState.Normal ?: return
        val availableTags = currentState.availableTags

        val tagNames = availableTags.map { it.name }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.manage_tags)
            .setItems(tagNames) { _, which ->
                showEditTagDialog(availableTags[which])
            }
            .setPositiveButton(R.string.add_tag) { _, _ ->
                showCreateTagDialog()
            }
            .setNegativeButton(R.string.ok, null)
            .show()
    }

    private fun showCreateTagDialog() {
        val input = EditText(requireContext())
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.space_medium)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.space_medium)
        input.layoutParams = params
        input.hint = getString(R.string.tag_name_hint)
        container.addView(input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_tag)
            .setView(container)
            .setPositiveButton(R.string.ok) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    vm.onCreateTag(name)
                    // Re-open manage dialog to see changes or add more
                    // Ideally we'd wait for flow update but for simplicity just closing or letting user reopen
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                vm.onManageTagsSelected()
            }
            .show()
    }

    private fun showEditTagDialog(tag: Tag) {
        val input = EditText(requireContext())
        input.setText(tag.name)
        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.space_medium)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.space_medium)
        input.layoutParams = params
        input.hint = getString(R.string.tag_name_hint)
        container.addView(input)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename_tag)
            .setView(container)
            .setPositiveButton(R.string.ok) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank() && name != tag.name) {
                    vm.onRenameTag(tag.id, name)
                }
                vm.onManageTagsSelected()
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                 showDeleteTagConfirmationDialog(tag)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                vm.onManageTagsSelected()
            }
            .show()
    }

    private fun showDeleteTagConfirmationDialog(tag: Tag) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.warning)
            .setMessage(R.string.delete_tag_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                vm.onDeleteTag(tag.id)
                vm.onManageTagsSelected()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                showEditTagDialog(tag)
            }
            .show()
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
