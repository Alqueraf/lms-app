package com.instructure.candroid.activity

import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.models.EditableFile

class ViewMediaActivity : BaseViewMediaActivity() {
    override fun allowCopyingUrl() = false
    override fun allowEditing() = false
    override fun handleEditing(editableFile: EditableFile) {}
}