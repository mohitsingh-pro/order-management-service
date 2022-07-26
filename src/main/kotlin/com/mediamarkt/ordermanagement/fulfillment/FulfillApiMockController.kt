package com.mediamarkt.ordermanagement.fulfillment

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import kotlin.random.Random

@Controller("/")
open class FulfillApiMockController() {

    @Put(value = "/fulfillment/{orderNumber}")
    open fun processOrder(@QueryValue orderNumber: String): HttpResponse<String> {
        return HttpResponse.ok(buildRandomResponse())
    }

    companion object {
        private fun buildRandomResponse() = if (Random.nextInt(1, 100) % 2 == 0) {
            "success"
        } else {
            "failure"
        }
    }
}
