package com.instructure.pandautils.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.dismissExisting
import kotlin.properties.Delegates


class FileExistsDialog : DialogFragment() {

    var fileName: String by StringArg()
    private var replaceCallback: () -> Unit by Delegates.notNull()

    init {
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
                .setTitle(R.string.fileExists)
                .setMessage("The file $fileName exists in the downloads directory already. Replace it?")
                .setPositiveButton(R.string.replace) { _, _ -> replaceCallback() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        const val FILENAME = "filename"
        fun newInstance(fileName: String, callback: () -> Unit): FileExistsDialog = FileExistsDialog().apply {
            this.fileName = fileName
            replaceCallback = callback
        }

        fun show(fragmentManager: FragmentManager, fileName: String, callback: () -> Unit) {
            fragmentManager.dismissExisting<FileExistsDialog>()
            newInstance(fileName, callback).show(fragmentManager, FileExistsDialog::class.java.simpleName)
        }
    }
}