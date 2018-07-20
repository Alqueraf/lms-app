import com.instructure.dataseeding.util.Certs
import com.instructure.dataseeding.util.Config
import io.grpc.ManagedChannel
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder

object Client {
    fun buildChannel(): ManagedChannel {
        val sslContext = Config.clientSslContext(
                Certs.caCert,
                Certs.clientCert,
                Certs.clientPrivateKey)

        return NettyChannelBuilder.forAddress(Config.exampleDotCom)
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext)
                .overrideAuthority(Config.exampleDotCom.hostName)
                .build()
    }
}
