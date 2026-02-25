package com.michaeltroger.gruenerpass.certificates

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.certificates.search.SearchQueryTextListener
import com.michaeltroger.gruenerpass.certificates.states.ViewState

class CertificatesMenuProvider(
    private val context: Context,
    private val vm: CertificatesViewModel,
    private val isListLayout: Boolean = false,
) : MenuProvider {
    private var searchView: SearchView? = null
    private var menu: Menu? = null

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
        this.menu = menu

        val searchMenuItem = menu.findItem(R.id.search)
        searchView = searchMenuItem.actionView as SearchView
        searchView?.queryHint = context.getString(R.string.search_query_hint)
        restorePendingSearchQueryFilter(searchMenuItem)
        searchView?.setOnQueryTextListener(SearchQueryTextListener {
            vm.onSearchQueryChanged(it)
        })

        updateMenuState(vm.viewState.value)
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.add -> {
            vm.onAddFileSelected()
            true
        }

        R.id.warning -> {
            vm.onShowWarningDialogSelected()
            true
        }

        R.id.pro -> {
            vm.onGetPro()
            true
        }

        R.id.openMore -> {
            vm.onShowMoreSelected()
            true
        }

        R.id.openSettings -> {
            vm.onShowSettingsSelected()
            true
        }

        R.id.deleteFiltered -> {
            vm.onDeleteFilteredSelected()
            true
        }

        R.id.deleteAll -> {
            vm.onDeleteAllSelected()
            true
        }

        R.id.lock -> {
            vm.lockApp()
            true
        }

        R.id.export_filtered -> {
            vm.onExportFilteredSelected()
            true
        }

        R.id.toggleBarcodeSize -> {
            vm.toggleBarcodeSize()
            true
        }

        R.id.export_all -> {
            vm.onExportAllSelected()
            true
        }

        R.id.scrollToFirst -> {
            vm.onScrollToFirstSelected()
            true
        }

        R.id.scrollToLast -> {
            vm.onScrollToLastSelected()
            true
        }

        R.id.changeOrder -> {
            vm.onChangeOrderSelected()
            true
        }

        R.id.switchLayout -> {
            vm.onSwitchLayoutSelected()
            true
        }

        R.id.filter_tags -> {
            vm.onFilterTagsSelected()
            true
        }

        else -> false
    }

    fun onPause() {
        searchView?.setOnQueryTextListener(null) // avoids an empty string to be sent
    }

    private fun restorePendingSearchQueryFilter(searchMenuItem: MenuItem) {
        val pendingFilter = (vm.viewState.value as? ViewState.Normal)?.filterSearchText ?: return
        if (pendingFilter.isNotEmpty()) {
            searchMenuItem.expandActionView()
            searchView?.setQuery(pendingFilter, false)
            searchView?.clearFocus()
        }
    }

    fun updateMenuState(state: ViewState) {
        menu?.apply {
            findItem(R.id.add)?.isVisible = state.showAddMenuItem
            findItem(R.id.warning)?.isVisible = state.showWarningButton
            findItem(R.id.openSettings)?.isVisible = state.showSettingsMenuItem
            findItem(R.id.pro)?.isVisible = state.showGetProMenuItem
            findItem(R.id.deleteAll)?.isVisible = state.showDeleteAllMenuItem
            findItem(R.id.deleteFiltered)?.isVisible = state.showDeleteFilteredMenuItem
            findItem(R.id.lock)?.isVisible = state.showLockMenuItem
            findItem(R.id.export_all)?.isVisible = state.showExportAllMenuItem
            findItem(R.id.export_filtered)?.isVisible = state.showExportFilteredMenuItem
            findItem(R.id.changeOrder)?.isVisible = state.showChangeOrderMenuItem
            findItem(R.id.scrollToFirst)?.isVisible = if (isListLayout) {
                false
            } else {
                state.showScrollToFirstMenuItem
            }
            findItem(R.id.scrollToLast)?.isVisible = if (isListLayout) {
                false
            } else {
                state.showScrollToLastMenuItem
            }
            findItem(R.id.search)?.apply {
                isVisible = state.showSearchMenuItem
                if (!state.showSearchMenuItem) {
                    collapseActionView()
                }
                if (state is ViewState.Normal) {
                    if (state.filterSearchText.isEmpty() && state.filterTagNames.isEmpty()) {
                        collapseActionView()
                    }
                }
            }
            findItem(R.id.filter_tags)?.isVisible = state.showSearchMenuItem || state.showAddMenuItem // Show if search or add is allowed (likely docs present)
            findItem(R.id.openMore)?.isVisible = state.showMoreMenuItem
            findItem(R.id.switchLayout)?.isVisible = state.showSwitchLayoutMenuItem
            findItem(R.id.toggleBarcodeSize)?.isVisible = if (isListLayout) {
                false
            } else {
                state.showToggleBarcodeSizeMenuItem
            }
        }
    }
}
