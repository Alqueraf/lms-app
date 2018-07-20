package com.instructure.dataseeding.seedyimpls

import com.instructure.dataseeding.Reaper
import com.instructure.dataseeding.SeedyReaper
import com.instructure.dataseeding.api.ObserverApi
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.soseedy.*
import io.grpc.stub.StreamObserver

class SeedyObserverImpl : SeedyObserversGrpc.SeedyObserversImplBase(), Reaper by SeedyReaper {
    //region API Calls
    private fun addObserveeWithCredentials(loginId: String, password: String, token: String) =
        ObserverApi.addObserverWithCredentials(loginId, password, token)

    private fun getObserverAlertThresholds(token: String) =
        ObserverApi.getObserverAlertThresholds(token)

    private fun addObserverAlertThreshold(alertType: String, userId: Long, observerId: Long, token: String) =
        ObserverApi.addObserverAlertThreshold(alertType, userId, observerId, token)

    private fun getObserverAlerts(userId: Long, token: String) =
        ObserverApi.getObserverAlerts(userId, token)

    //endregion

    override fun addObserveeWithCredentials(request: AddObserveeWithCredentialsRequest, responseObserver: StreamObserver<CanvasUser>) {
        try {
            val observee = addObserveeWithCredentials(request.loginId, request.password, request.observerToken)
            val reply = CanvasUser.newBuilder()
                    .setId(observee.id)
                    .setLoginId(request.loginId)
                    .setPassword(request.password)
                    .setDomain(CanvasRestAdapter.canvasDomain)
                    .setToken(request.observeeToken)
                    .setName(observee.name)
                    .setShortName(observee.shortName)
                    .setAvatarUrl("")
                    .build()

            onSuccess(responseObserver, reply)
        } catch (e: Exception) {
            onError(responseObserver, e)
        }
    }

    override fun getObserverAlertThresholds(request: GetObserverAlertThresholdsRequest, responseObserver: StreamObserver<ObserverAlertThresholds>) {
        try {
            val thresholds = getObserverAlertThresholds(request.token)
            val reply = ObserverAlertThresholds.newBuilder()
                    .addAllThresholds(thresholds.map {
                        ObserverAlertThreshold.newBuilder()
                                .setId(it.id)
                                .setAlertType(it.alertType)
                                .setThreshold(it.threshold)
                                .setWorkflowState(it.workflowState)
                                .setUserId(it.userId)
                                .setObserverId(it.observerId)
                                .build()
                    })

            onSuccess(responseObserver, reply.build())
        } catch (e: Exception) {
            onError(responseObserver, e)
        }
    }

    override fun addObserverAlertThreshold(request: AddObserverAlertThresholdRequest, responseObserver: StreamObserver<ObserverAlertThreshold>) {
        try {
            val threshold = addObserverAlertThreshold(request.alertType, request.userId, request.observerId, request.token)
            val reply = ObserverAlertThreshold.newBuilder()
                    .setId(threshold.id)
                    .setAlertType(threshold.alertType)
                    .setThreshold(threshold.threshold)
                    .setWorkflowState(threshold.workflowState)
                    .setUserId(threshold.userId)
                    .setObserverId(threshold.observerId)
                    .build()
            onSuccess(responseObserver, reply)
        } catch (e: Exception) {
            onError(responseObserver, e)
        }
    }

    override fun getObserverAlerts(request: GetObserverAlertsRequest, responseObserver: StreamObserver<ObserverAlerts>) {
        try {
            val alerts = getObserverAlerts(request.userId, request.token)
            val reply = ObserverAlerts.newBuilder()
                    .addAllAlerts(alerts.map {
                        ObserverAlert.newBuilder()
                                .setId(it.id)
                                .setObserverAlertThresholdId(it.observerAlertThresholdId)
                                .setContextType(it.contextType)
                                .setContextId(it.contextId)
                                .setAlertType(it.alertType)
                                .setWorkflowState(it.workflowState)
                                .setTitle(it.title)
                                .setUserId(it.userId)
                                .setObserverId(it.observerId)
                                .setHtmlUrl(it.htmlUrl)
                                .build()
                    })
            onSuccess(responseObserver, reply.build())
        } catch (e: Exception) {
            onError(responseObserver, e)
        }
    }
}
