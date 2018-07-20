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

import com.instructure.dataseeding.seedyimpls.*
import com.instructure.dataseeding.util.Certs
import com.instructure.dataseeding.util.Config
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.netty.NettyServerBuilder
import java.io.IOException

object OutOfProcessServer {
    private val sslContext = Config.serverSslContext(
            Certs.serverCert,
            Certs.serverPrivateKey,
            Certs.caCert)

    private val server: Server = NettyServerBuilder.forAddress(Config.localhost)
            .addService(GeneralSeedyImpl())
            .addService(SeedyAssignmentsImpl())
            .addService(SeedyConversationsImpl())
            .addService(SeedyCoursesImpl())
            .addService(SeedyDiscussionsImpl())
            .addService(SeedyEnrollmentsImpl())
            .addService(SeedyFilesImpl())
            .addService(SeedyGradingPeriodsImpl())
            .addService(SeedyGroupsImpl())
            .addService(SeedyPagesImpl())
            .addService(SeedyQuizzesImpl())
            .addService(SeedySectionsImpl())
            .addService(SeedyUsersImpl())
            .sslContext(sslContext)
            .build()

    @Throws(IOException::class)
    fun start(): Server {
        server.start()
        println("Server started on port ${server.port}")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("JVM shutdown hook activated. Shutting down...")
                OutOfProcessServer.stop()
                System.err.println("Server shut down.")
            }
        })

        return server
    }

    private fun stop() {
        server.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server.awaitTermination()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        OutOfProcessServer.start()
        OutOfProcessServer.blockUntilShutdown()
    }
}
