package com.instructure.canvasapi2.pact.canvas

import android.content.Context
import au.com.dius.pact.consumer.ConsumerPactTestMk2
import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.model.PactSpecVersion
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger
import org.junit.Before
import org.mockito.Mockito

abstract class CanvasPact : ConsumerPactTestMk2() {

    companion object {
        const val providerName = "Canvas LMS API"
        const val consumerName = "Android"
    }

    override fun providerName(): String = providerName
    override fun consumerName(): String = consumerName

    fun getParams(mockServer: MockServer): RestParams {
        return RestParams.Builder()
                .withDomain(mockServer.getUrl())
                .build() ?: throw RuntimeException("Unable to build rest params")
    }

    @Before
    fun setUp() {
        RestBuilder.isPact = true
        ContextKeeper.appContext = Mockito.mock(Context::class.java)
        Logger.IS_LOGGING = false
    }

    override fun getSpecificationVersion(): PactSpecVersion {
        return PactSpecVersion.V2
    }
}
