package com.instructure.dataseeding.seedyimpls

import com.instructure.dataseeding.Reaper
import com.instructure.dataseeding.SeedyReaper
import com.instructure.dataseeding.api.ModulesApi
import com.instructure.soseedy.CreateModuleRequest
import com.instructure.soseedy.Module
import com.instructure.soseedy.SeedyModulesGrpc.SeedyModulesImplBase
import com.instructure.soseedy.UpdateModuleRequest
import io.grpc.stub.StreamObserver

class SeedyModulesImpl : SeedyModulesImplBase(), Reaper by SeedyReaper {
    //region API Calls
    private fun createModule(courseId: Long, teacherToken: String, unlockAt: String?) =
            ModulesApi.createModule(courseId, teacherToken, unlockAt)

    private fun updateModule(courseId: Long, id: Long, published: Boolean, teacherToken: String) =
            ModulesApi.updateModule(courseId, id, published, teacherToken)
    //endregion

    override fun createModule(request: CreateModuleRequest, responseObserver: StreamObserver<Module>) {
        try {
            val module = createModule(request.courseId, request.token, request.unlockAt)
            val reply = Module.newBuilder()
                    .setId(module.id)
                    .setName(module.name)

            // API may return null values, even though the IDE says this will never be null
            if (module.unlockAt != null) {
                reply.unlockAt = module.unlockAt
            }

            onSuccess(responseObserver, reply.build())
        } catch (e: Exception) {
            onError(responseObserver, e)
        }
    }

    override fun updateModule(request: UpdateModuleRequest, responseObserver: StreamObserver<Module>) {
        try {
            val module = updateModule(request.courseId, request.id, request.published, request.token)
            val reply = Module.newBuilder()
                    .setId(module.id)
                    .setName(module.name)
                    .setPublished(module.published)

            // API may return null values, even though the IDE says this will never be null
            if (module.unlockAt != null) {
                reply.unlockAt = module.unlockAt
            }

            onSuccess(responseObserver, reply.build())
        } catch (e: Exception) {
            onError(responseObserver, e)
        }
    }
}
