package com.michaeltroger.gruenerpass.certificates.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.michaeltroger.gruenerpass.R
import com.michaeltroger.gruenerpass.certificates.documentorder.DocumentOrderItem
import com.michaeltroger.gruenerpass.db.Certificate
import com.michaeltroger.gruenerpass.db.Tag
import com.xwray.groupie.GroupieAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
interface CertificateDialogs {
    fun closeAllDialogs()
    fun showEnterPasswordDialog(
        context: Context,
        onPasswordEntered: (String) -> Unit,
        onCancelled: () -> Unit
    )
    fun showDoYouWantToDeleteDialog(context: Context, id: String, onDeleteConfirmed: (String) -> Unit)
    fun showDoYouWantToDeleteAllDialog(context: Context, onDeleteAllConfirmed: () -> Unit)
    fun showDoYouWantToDeleteFilteredDialog(context: Context, documentCount: Int, onDeleteFilteredConfirmed: () -> Unit)
    fun showWarningDialog(context: Context)
    fun showChangeDocumentNameDialog(
        context: Context,
        originalDocumentName: String,
        onDocumentNameChanged: (String) -> Unit
    )
    fun showChangeDocumentOrder(
        context: Context,
        scope: CoroutineScope,
        originalOrder: List<Certificate>,
        onOrderChanged: (List<String>) -> Unit
    ): Job
    fun showFilterTagsDialog(
        context: Context,
        availableTags: List<Tag>,
        activeTagIds: Set<Long>,
        onTagFilterToggled: (Long) -> Unit,
        onManageTagsClicked: () -> Unit
    )
    @Suppress("LongParameterList")
    fun showAssignTagsDialog(
        context: Context,
        certificateId: String,
        availableTags: List<Tag>,
        assignedTagIds: Set<Long>,
        onManageTagsClicked: () -> Unit,
        onTagsAssigned: (String, List<Long>) -> Unit
    )
    fun showManageTagsDialog(
        context: Context,
        availableTags: List<Tag>,
        onEditTagClicked: (Tag) -> Unit,
        onCreateTagClicked: () -> Unit
    )
    fun showCreateTagDialog(
        context: Context,
        onTagCreated: (String) -> Unit,
        onCancel: () -> Unit
    )
    fun showEditTagDialog(
        context: Context,
        tag: Tag,
        onTagRenamed: (Long, String) -> Unit,
        onDeleteTagClicked: (Long) -> Unit,
        onCancel: () -> Unit
    )
    fun showDeleteTagConfirmationDialog(
        context: Context,
        tag: Tag,
        onDeleteTagConfirmed: (Long) -> Unit,
        onCancel: () -> Unit
    )
}

@Suppress("TooManyFunctions")
class CertificateDialogsImpl @Inject constructor() : CertificateDialogs {

    private var dialog: Dialog? = null

    override fun showDoYouWantToDeleteAllDialog(context: Context, onDeleteAllConfirmed: () -> Unit) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setMessage(R.string.dialog_delete_all_confirmation_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                onDeleteAllConfirmed()
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showDoYouWantToDeleteFilteredDialog(
        context: Context,
        documentCount: Int,
        onDeleteFilteredConfirmed: () -> Unit
    ) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setMessage(context.resources.getQuantityString(
                R.plurals.dialog_delete_filtered_confirmation_text,
                documentCount,
                documentCount
            ))
            .setPositiveButton(R.string.ok) { _, _ ->
                onDeleteFilteredConfirmed()
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showWarningDialog(context: Context) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_warning_title)
            .setMessage(R.string.dialog_warning_description)
            .setPositiveButton(R.string.ok, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showDoYouWantToDeleteDialog(context: Context, id: String, onDeleteConfirmed: (String) -> Unit) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setMessage(R.string.dialog_delete_confirmation_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                onDeleteConfirmed(id)
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showChangeDocumentNameDialog(
        context: Context,
        originalDocumentName: String,
        onDocumentNameChanged: (String) -> Unit
    ) {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_document_name, null, false)

        val textField = customAlertDialogView.findViewById<TextInputLayout>(R.id.document_name_text_field).apply {
            editText!!.setText(originalDocumentName)
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_document_name_title)
            .setView(customAlertDialogView)
            .setPositiveButton(R.string.ok) { _, _ ->
                onDocumentNameChanged(textField.editText!!.text.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showChangeDocumentOrder(
        context: Context,
        scope: CoroutineScope,
        originalOrder: List<Certificate>,
        onOrderChanged: (List<String>) -> Unit
    ) = scope.launch {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_document_order, null, false)

        val myAdapter = GroupieAdapter()
        customAlertDialogView.findViewById<RecyclerView>(R.id.document_order).adapter = myAdapter
        val listFlow: MutableStateFlow<List<Certificate>> = MutableStateFlow(originalOrder)

        fun onUpClicked(id: String) {
            val index = listFlow.value.indexOfFirst {
                it.id == id
            }
            val newState = listFlow.value.toMutableList()
            newState[index] = listFlow.value.getOrNull(index - 1) ?: return
            newState[index - 1] = listFlow.value[index]
            listFlow.value = newState
        }

        fun onDownClicked(id: String) {
            val index = listFlow.value.indexOfFirst {
                it.id == id
            }
            val newState = listFlow.value.toMutableList()
            newState[index] = listFlow.value.getOrNull(index + 1) ?: return
            newState[index + 1] = listFlow.value[index]
            listFlow.value = newState
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_document_order_title)
            .setView(customAlertDialogView)
            .setPositiveButton(R.string.ok) { _, _ ->
                onOrderChanged(listFlow.value.map { it.id })
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                this@CertificateDialogsImpl.dialog = null
            }
            .create()
        this@CertificateDialogsImpl.dialog = dialog
        dialog.show()

        listFlow.collect { list ->
            val items = list.map { certificate ->
                DocumentOrderItem(
                    fileName = certificate.id,
                    documentName = certificate.name,
                    onDownClicked = ::onDownClicked,
                    onUpClicked = ::onUpClicked
                )
            }
            myAdapter.update(items)
        }
    }

    override fun showEnterPasswordDialog(
        context: Context,
        onPasswordEntered: (String) -> Unit,
        onCancelled: () -> Unit,
    ) {
        val customAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_password, null, false)

        val passwordTextField = customAlertDialogView.findViewById<TextInputLayout>(R.id.password_text_field)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_password_protection_title)
            .setView(customAlertDialogView)
            .setPositiveButton(R.string.ok) { _, _ ->
                onPasswordEntered(passwordTextField.editText!!.text.toString())
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                onCancelled()
            }
            .setOnCancelListener {
                onCancelled()
            }
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showFilterTagsDialog(
        context: Context,
        availableTags: List<Tag>,
        activeTagIds: Set<Long>,
        onTagFilterToggled: (Long) -> Unit,
        onManageTagsClicked: () -> Unit
    ) {
        val tagNames = availableTags.map { it.name }.toTypedArray()
        val checkedItems = availableTags.map { it.id in activeTagIds }.toBooleanArray()

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_filter_by_tags_title)
            .setMultiChoiceItems(tagNames, checkedItems) { _, which, isChecked ->
                onTagFilterToggled(availableTags[which].id)
            }
            .setNeutralButton(R.string.manage_tags_dialog_text) { _, _ ->
                onManageTagsClicked()
            }
            .setPositiveButton(R.string.ok, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    @Suppress("LongParameterList")
    override fun showAssignTagsDialog(
        context: Context,
        certificateId: String,
        availableTags: List<Tag>,
        assignedTagIds: Set<Long>,
        onManageTagsClicked: () -> Unit,
        onTagsAssigned: (String, List<Long>) -> Unit
    ) {
        val tagNames = availableTags.map { it.name }.toTypedArray()
        val checkedItems = availableTags.map { it.id in assignedTagIds }.toBooleanArray()
        val selectedTagIds = assignedTagIds.toMutableSet()

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.dialog_assign_tags_title)
            .setMultiChoiceItems(tagNames, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    selectedTagIds.add(availableTags[which].id)
                } else {
                    selectedTagIds.remove(availableTags[which].id)
                }
            }
            .setNeutralButton(R.string.manage_tags_dialog_text) { _, _ ->
                onManageTagsClicked()
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                onTagsAssigned(certificateId, selectedTagIds.toList())
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showManageTagsDialog(
        context: Context,
        availableTags: List<Tag>,
        onEditTagClicked: (Tag) -> Unit,
        onCreateTagClicked: () -> Unit
    ) {
        val tagNames = availableTags.map { it.name }.toTypedArray()

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.manage_tags_dialog_text)
            .setItems(tagNames) { _, which ->
                onEditTagClicked(availableTags[which])
            }
            .setPositiveButton(R.string.add_tag_dialog_text) { _, _ ->
                onCreateTagClicked()
            }
            .setNegativeButton(R.string.ok, null)
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showCreateTagDialog(
        context: Context,
        onTagCreated: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        val input = EditText(context)
        val container = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = context.resources.getDimensionPixelSize(R.dimen.space_medium)
        params.leftMargin = margin
        params.rightMargin = margin
        input.layoutParams = params
        input.hint = context.getString(R.string.dialog_tag_name_hint)
        container.addView(input)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.add_tag_dialog_text)
            .setView(container)
            .setPositiveButton(R.string.ok) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    onTagCreated(name)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                onCancel()
            }
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showEditTagDialog(
        context: Context,
        tag: Tag,
        onTagRenamed: (Long, String) -> Unit,
        onDeleteTagClicked: (Long) -> Unit,
        onCancel: () -> Unit
    ) {
        val input = EditText(context)
        input.setText(tag.name)
        val container = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = context.resources.getDimensionPixelSize(R.dimen.space_medium)
        params.leftMargin = margin
        params.rightMargin = margin
        input.layoutParams = params
        input.hint = context.getString(R.string.dialog_tag_name_hint)
        container.addView(input)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.rename_tag_dialog_text)
            .setView(container)
            .setPositiveButton(R.string.ok) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank() && name != tag.name) {
                    onTagRenamed(tag.id, name)
                }
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                onDeleteTagClicked(tag.id)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                onCancel()
            }
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun showDeleteTagConfirmationDialog(
        context: Context,
        tag: Tag,
        onDeleteTagConfirmed: (Long) -> Unit,
        onCancel: () -> Unit
    ) {
        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.warning)
            .setMessage(R.string.delete_tag_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                onDeleteTagConfirmed(tag.id)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                onCancel()
            }
            .setOnDismissListener {
                this.dialog = null
            }
            .create()
        this.dialog = dialog
        dialog.show()
    }

    override fun closeAllDialogs() {
        if (this.dialog?.isShowing == true) {
            this.dialog?.dismiss()
        }
        this.dialog = null
    }
}
