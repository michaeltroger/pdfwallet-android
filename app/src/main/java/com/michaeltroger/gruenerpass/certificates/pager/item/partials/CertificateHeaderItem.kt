package com.michaeltroger.gruenerpass.certificates.pager.item.partials

import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.databinding.ItemCertificatePartialHeaderBinding
import com.michaeltroger.gruenerpass.db.Tag
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem

@Suppress("LongParameterList")
class CertificateHeaderItem(
    private val documentName: String,
    private val fileName: String,
    private val tags: List<Tag>,
    private val onDeleteCalled: () -> Unit,
    private val onDocumentNameClicked: () -> Unit,
    private val onShareCalled: () -> Unit,
    private val onAssignTagsClicked: () -> Unit,
) : BindableItem<ItemCertificatePartialHeaderBinding>() {

    override fun initializeViewBinding(view: View) = ItemCertificatePartialHeaderBinding.bind(view)
    override fun getLayout() = R.layout.item_certificate_partial_header

    override fun bind(viewBinding: ItemCertificatePartialHeaderBinding, position: Int) {
        viewBinding.apply {
            deleteIcon.setOnClickListener {
                onDeleteCalled()
            }
            shareIcon.setOnClickListener {
                onShareCalled()
            }
            tagIcon.setOnClickListener {
                onAssignTagsClicked()
            }
            name.text = documentName
            name.setOnClickListener {
                onDocumentNameClicked()
            }
            tagsChipGroup.removeAllViews()
            tags.forEach { tag ->
                val chip = Chip(root.context).apply {
                    text = tag.name
                    isCheckable = false
                    isClickable = false
                }
                tagsChipGroup.addView(chip)
            }
            tagsScrollView.isVisible = tags.isNotEmpty()
        }

    }

    override fun isSameAs(other: Item<*>): Boolean {
        return viewType == other.viewType
    }

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return (other as? CertificateHeaderItem)?.fileName == fileName
            && other.documentName == documentName
            && other.tags == tags
    }
}
