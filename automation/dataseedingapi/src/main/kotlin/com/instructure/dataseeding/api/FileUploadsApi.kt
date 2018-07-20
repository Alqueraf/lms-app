//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.dataseeding.api

import com.google.protobuf.ByteString
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.dataseeding.model.FileUploadParams
import com.instructure.dataseeding.model.StartFileUpload
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.soseedy.FileUploadType
import com.instructure.soseedy.FileUploadType.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

object FileUploadsApi {
    interface FileUploadsService {

        @POST("courses/{courseId}/assignments/{assignmentId}/submissions/self/files")
        fun assignmentSubmissionFileUpload(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body startFileUpload: StartFileUpload): Call<FileUploadParams>

        @POST("courses/{courseId}/assignments/{assignmentId}/submissions/self/comments/files")
        fun commentAttachmentFileUpload(
                @Path("courseId") courseId: Long,
                @Path("assignmentId") assignmentId: Long,
                @Body startFileUpload: StartFileUpload): Call<FileUploadParams>

        @Multipart
        @POST
        fun uploadFile(
                @Url url: String,
                @PartMap uploadParams: Map<String, @JvmSuppressWildcards RequestBody>,
                @Part file: MultipartBody.Part
        ): Call<AttachmentApiModel>
    }

    private fun fileUploadsService(token: String): FileUploadsService
            = CanvasRestAdapter.retrofitWithToken(token).create(FileUploadsService::class.java)

    private val noAuthUploadsService: FileUploadsService by lazy {
        CanvasRestAdapter.noAuthRetrofit.create(FileUploadsService::class.java)
    }

    /**
     * The Entry Point to File Uploading Glory
     *
     * Start here to upload a file!
     */
    fun uploadFile(courseId: Long,
                   assignmentId: Long,
                   file: ByteString,
                   fileName: String,
                   token: String,
                   fileUploadType: FileUploadType): AttachmentApiModel {

        // Get file upload params from Canvas, telling us where to upload the file
        val params = getFileUploadParams(courseId, assignmentId, file, fileName, fileUploadType, token)

        // Upload the file based on the params we got back; return the resulting attachment
        return uploadFileToCanvas(params, file)
    }

    private fun uploadFileToCanvas(fileUploadParams: FileUploadParams, file: ByteString): AttachmentApiModel {
        val requestFile = RequestBody.create(MediaType.parse("application/octet-stream"), file.toByteArray())
        val requestFilePart = MultipartBody.Part.createFormData("file", fileUploadParams.uploadParams.get("Filename"), requestFile)

        return noAuthUploadsService.uploadFile(
                fileUploadParams.uploadUrl,
                fileUploadParams.uploadParams.mapValues
                { RequestBody.create(MediaType.parse("text/plain"), it.value) },
                requestFilePart)
                .execute()
                .body()!!
    }

    private fun getFileUploadParams(courseId: Long,
                                    assignmentId: Long,
                                    file: ByteString,
                                    fileName: String,
                                    fileUploadType: FileUploadType,
                                    token: String): FileUploadParams {

        val request = StartFileUpload(fileName, file.size().toLong())

        return when (fileUploadType) {
            ASSIGNMENT_SUBMISSION -> fileUploadsService(token).assignmentSubmissionFileUpload(courseId, assignmentId, request)
            COMMENT_ATTACHMENT -> fileUploadsService(token).commentAttachmentFileUpload(courseId, assignmentId, request)
            UNRECOGNIZED -> TODO()
        }.execute().body()!!
    }
}
