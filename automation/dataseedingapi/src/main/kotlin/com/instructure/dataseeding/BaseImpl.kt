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

package com.instructure.dataseeding

import io.grpc.Status
import io.grpc.stub.StreamObserver

interface Reaper {
    fun <V> onError(responseObserver: StreamObserver<V>?, e: Exception)
    fun <V> onSuccess(responseObserver: StreamObserver<V>?, reply: V)
}

object SeedyReaper : Reaper {

    override fun <V> onError(responseObserver: StreamObserver<V>?, e: Exception) {
        if (responseObserver == null) return

        responseObserver.onError(Status.INTERNAL
                .withDescription(e.toString())
                .withCause(e)
                .asRuntimeException())
    }

    override fun <V> onSuccess(responseObserver: StreamObserver<V>?, reply: V) {
        if (responseObserver == null) return

        responseObserver.onNext(reply)
        responseObserver.onCompleted()
    }
}
