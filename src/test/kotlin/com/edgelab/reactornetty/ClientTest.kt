package com.edgelab.reactornetty

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.kotlin.test.test
import reactor.netty.http.client.PrematureCloseException
import java.nio.channels.NotYetConnectedException
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ClientTest(@Autowired val client: Client) {

    private val count = 100_000

    @Test
    fun `test PrematureCloseException`() {
        client.prematureCloseException(count).test()
            .thenAwait(Duration.ofSeconds(5))
            .thenCancel()
            .verifyThenAssertThat()
            .hasOperatorErrors()
            .hasOperatorErrorsSatisfying {
                assertThat(it.mapNotNull { tuple -> tuple.t1.orElse(null) })
                    .hasOnlyElementsOfType(PrematureCloseException::class.java)
            }
    }

    @Test
    fun `test NotYetConnectedException`() {
        client.notYetConnectedException(count).test()
            .verifyError(NotYetConnectedException::class.java)
    }

}