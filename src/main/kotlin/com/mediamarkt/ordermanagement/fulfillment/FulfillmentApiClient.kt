package com.mediamarkt.ordermanagement.fulfillment

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.netty.DefaultHttpClient
import jakarta.inject.Singleton
import java.net.URI


@Singleton
class FulfillmentApiClient() {

    fun fulfillOrder(orderNumber: Long): String? {
        val httpRequest = HttpRequest.PUT("/fulfillment/${orderNumber}", null)
        val response = httpClient.exchange(httpRequest, String::class.java)
        return response.body()
    }

    companion object {
        private val httpClient = buildHttpClient()
        private fun buildHttpClient(): BlockingHttpClient {
            val configuration = DefaultHttpClientConfiguration()
            return DefaultHttpClient(URI.create("http://localhost:8080"), configuration).toBlocking()
        }
    }
}

