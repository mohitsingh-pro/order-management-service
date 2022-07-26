package com.mediamarkt.ordermanagement.payment

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue

@Controller("/")
open class PaymentMockController() {

    @Get(value = "/payment/{orderNumber}")
    open fun processOrder(@QueryValue orderNumber: String): HttpResponse<String> {
        return HttpResponse.ok("Order created successfully with order number : $orderNumber, please proceed to payment")
    }

}
