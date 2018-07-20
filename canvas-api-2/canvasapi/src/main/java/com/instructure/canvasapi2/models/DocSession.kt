/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel
import paperparcel.PaperParcelable


@PaperParcel
data class DocSession(
        @SerializedName("document_id")
        var documentId: String,
        @SerializedName("urls")
        var annotationUrls: AnnotationUrls,
        @SerializedName("annotations")
        var annotationMetadata: AnnotationMetadata,
        @SerializedName("panda_push")
        var pandaPush: PandaPush? = null
) : PaperParcelable {

    lateinit var apiValues: ApiValues

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelDocSession.CREATOR
    }
}

@PaperParcel
data class AnnotationMetadata(
        var enabled: Boolean,
        @SerializedName("user_name")
        var userName: String,
        @SerializedName("user_id")
        var userId: String,
        var permissions: String? = null) : PaperParcelable {

    /*
        The permission field is given to us as a string with the form:
        "readwritemanage"
        "readwrite"
        "read"
        -undefined/null (no permission present)
    */

    fun canRead() : Boolean {
        return permissions?.contains("read") ?: false
    }

    fun canWrite() : Boolean {
        return permissions?.contains("write") ?: false
    }

    fun canManage() : Boolean {
        return permissions?.contains("manage") ?: false
    }

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelAnnotationMetadata.CREATOR
    }

}


@PaperParcel
data class AnnotationUrls(
        @SerializedName("pdf_download")
        var pdfDownload: String,
        @SerializedName("annotated_pdf_download")
        var annotatedPdfDownload: String
) : PaperParcelable {

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelAnnotationUrls.CREATOR
    }
}

@PaperParcel
data class PandaPush(
        @SerializedName("document_channel")
        var documentChannel: String? = null
) : PaperParcelable {

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelPandaPush.CREATOR
    }
}

@PaperParcel
data class ApiValues(
        val documentId: String,
        val pdfUrl: String,
        val sessionId: String,
        val canvaDocsDomain: String
) : PaperParcelable {

    companion object {
        @Suppress("unresolved_reference")
        @JvmField val CREATOR = PaperParcelApiValues.CREATOR
    }
}

