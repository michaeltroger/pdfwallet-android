package com.michaeltroger.gruenerpass.certificatedetails

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.barcode.BarcodeRenderer
import com.michaeltroger.gruenerpass.certificatedetails.states.DetailsViewState
import com.michaeltroger.gruenerpass.certificates.dialogs.CertificateDialogs
import com.michaeltroger.gruenerpass.certificates.pager.item.CertificateItem
import com.michaeltroger.gruenerpass.certificates.sharing.PdfSharing
import com.michaeltroger.gruenerpass.certificates.states.ViewEvent
import com.michaeltroger.gruenerpass.databinding.FragmentCertificateDetailsBinding
import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.settings.BarcodeSearchMode
import com.xwray.groupie.GroupieAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import javax.inject.Inject

@AndroidEntryPoint
class CertificateDetailsFragment : Fragment(R.layout.fragment_certificate_details), MenuProvider {

    private val vm by viewModels<CertificateDetailsViewModel>()

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val thread = newSingleThreadContext("RenderContext")

    private val adapter = GroupieAdapter()

    private var binding: FragmentCertificateDetailsBinding? = null

    @Inject
    lateinit var pdfSharing: PdfSharing
    @Inject
    lateinit var certificateDialogs: CertificateDialogs
    @Inject
    lateinit var barcodeRenderer: BarcodeRenderer
    private var menu: Menu? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding = FragmentCertificateDetailsBinding.bind(view)
        val binding = binding!!

        binding.certificateFullscreen.layoutManager = object : LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false,
        ) {
            override fun canScrollVertically(): Boolean {
                return false
            }

            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
        binding.certificateFullscreen.adapter = adapter

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
            ViewEvent.ShowGetPro -> findNavController().navigate(
                deepLink = "app://billing".toUri()
            )
            else -> {
                // do nothing
            }
        }
    }

    override fun onDestroyView() {
        binding?.certificateFullscreen?.adapter = null
        binding = null
        super.onDestroyView()
    }

    private fun updateState(state: DetailsViewState) {
        updateMenuState(state)
        when (state) {
            is DetailsViewState.Normal -> showCertificateState(
                certificate = state.document,
                searchBarcode = state.searchBarcode,
                invertColors = state.invertColors,
                showBarcodesHalfSize = state.showBarcodesHalfSize,
                generateNewBarcode = state.generateNewBarcode
            )
            is DetailsViewState.Deleted -> {
                findNavController().popBackStack()
            }
            DetailsViewState.Initial -> {
                // nothing to do
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun showCertificateState(
        certificate: Certificate,
        searchBarcode: BarcodeSearchMode,
        invertColors: Boolean,
        showBarcodesHalfSize: Boolean,
        generateNewBarcode: Boolean,
    ) {
        val item = CertificateItem(
            requireContext().applicationContext,
            fileName = certificate.id,
            isDetailView = true,
            barcodeRenderer = barcodeRenderer,
            documentName = certificate.name,
            searchBarcode = searchBarcode,
            invertColors = invertColors,
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
            showBarcodesInHalfSize = showBarcodesHalfSize,
            generateNewBarcode = generateNewBarcode,
        )

        adapter.update(listOf(item))
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
        this.menu = menu

        updateMenuState(vm.viewState.value)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.pro -> {
            vm.onGetPro()
            true
        }
        R.id.toggleBarcodeSize -> {
            vm.toggleBarcodeSize()
            true
        }
        else -> false
    }

    private fun updateMenuState(state: DetailsViewState) {
        menu?.apply {
            findItem(R.id.pro)?.isVisible = state.showGetProMenuItem
            findItem(R.id.toggleBarcodeSize)?.isVisible = state.showToggleBarcodeSizeMenuItem
        }
    }
}
