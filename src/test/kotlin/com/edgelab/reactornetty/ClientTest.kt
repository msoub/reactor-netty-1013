package com.edgelab.reactornetty

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test
import reactor.netty.http.client.PrematureCloseException
import java.nio.channels.NotYetConnectedException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ClientTest(@Autowired val client: Client) {

    private val count = 10_000
    private val bulkSize = 500

    @Test
    fun `test PrematureCloseException`() {
        client.prematureCloseException(count, bulkSize).test()
            .verifyError(PrematureCloseException::class.java)
    }

    @Test
    fun `test NotYetConnectedException`() {
        client.notYetConnectedException(count, bulkSize).test()
//            .expectError(NotYetConnectedException::class.java)
            .verifyError(NotYetConnectedException::class.java)
    }
}