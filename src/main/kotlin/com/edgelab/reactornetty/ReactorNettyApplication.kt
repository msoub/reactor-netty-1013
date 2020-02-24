package com.edgelab.reactornetty

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.AbstractEnvironment
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.nio.charset.Charset
import kotlin.random.Random

@SpringBootApplication
class ReactorNettyApplication

fun main(args: Array<String>) {
    runApplication<ReactorNettyApplication>(*args)
}

@RestController
class Controller {

    @PostMapping("/1", produces = ["application/json"])
    fun post1() = response()

    @PostMapping("/2", produces = ["application/json"])
    fun post2() = response()

    private fun response() = "{}".toMono()

}

@Component
class Client(private val builder: WebClient.Builder, private val environment: AbstractEnvironment) {

    private val webClient1: WebClient by lazy {
        builder.baseUrl("http://localhost:${environment.getProperty("local.server.port")}/1").build()
    }

    private val webClient2: WebClient by lazy {
        builder.baseUrl("http://localhost:${environment.getProperty("local.server.port")}/2").build()
    }

    fun prematureCloseException(count: Int, bulkSize: Int): Flux<Any> {
        return Flux.range(0, count)
            .flatMap { retrieve() }
            .map { Tuples.of(it.t1, it.t1, it.t1) }
            .window(bulkSize).flatMap { bulk(it) }
    }

    fun notYetConnectedException(count: Int, bulkSize: Int): Flux<Any> {
        return requestOne()
            .flatMapMany { Flux.range(0, count) }
            .flatMap { retrieve() }
            .map { Tuples.of(it.t1, it.t1, it.t1) }
            .window(bulkSize).flatMap { bulk(it) }
    }

    private fun retrieve() = Mono.zip(requestOneOptionalResponse(), Mono.zip(
        requestTwoOptionalResponse(),
        requestTwoOptionalResponse()
    ))

    private fun requestOne() =
        webClient1
            .post().uri("/")
            .retrieve()
            .toBodilessEntity()

    private fun requestOneOptionalResponse() =
        requestOne()
            .optional()

    private fun requestTwoOptionalResponse() =
        webClient2
            .post().uri("/")
            .retrieve()
            .toBodilessEntity()
            .optional()

    private fun <T> Mono<T>.optional(): Mono<String> {
        val randomString = Random.nextBytes(10).toString(Charset.defaultCharset())
        return this.flatMap {
            if (Random.nextBoolean()) Mono.empty()
            else Mono.just(randomString)
        }
    }

    private fun bulk(flux: Flux<Tuple3<String, String, String>>) =
        flux.collectList()
            .flatMap { requestOne() }
}
