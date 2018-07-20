import com.instructure.dataseeding.OutOfProcessServer
import com.instructure.soseedy.HealthCheckRequest
import com.instructure.soseedy.SeedyGeneralGrpc
import org.junit.Test

class OutOfProcessServerTest {

    @Test
    fun testServerAndClient() {
        val server = OutOfProcessServer.start()

        try {
            val channel = Client.buildChannel()
            val blockingStub = SeedyGeneralGrpc.newBlockingStub(channel)
            blockingStub.getHealthCheck(HealthCheckRequest.getDefaultInstance())
        } finally {
            server.shutdown()
            server.awaitTermination()
        }
    }
}
