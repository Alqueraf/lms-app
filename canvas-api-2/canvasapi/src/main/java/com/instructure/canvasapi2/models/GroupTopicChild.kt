package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable

@PaperParcel
data class GroupTopicChild(
        val id: Long,
        @SerializedName("group_id")
        val groupId: Long
) : PaperParcelable {
    companion object {
        @Suppress("UNRESOLVED_REFERENCE")
        @JvmField
        val CREATOR = PaperParcelGroupTopicChild.CREATOR
    }
}