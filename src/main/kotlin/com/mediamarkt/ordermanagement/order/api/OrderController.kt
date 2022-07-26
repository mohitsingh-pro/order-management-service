package com.mediamarkt.ordermanagement.order.api

import com.mediamarkt.ordermanagement.fulfillment.FulfillmentApiClient
import com.mediamarkt.ordermanagement.order.OrderStatus
import com.mediamarkt.ordermanagement.order.service.OrderService
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.net.URI
import javax.validation.Valid
import javax.validation.ValidationException


@Validated
@Controller("/")
class OrderController(val orderService: OrderService, val fulfillmentApiClient: FulfillmentApiClient) {

    @Post(value = "/order")
    fun saveOrder(
        @Body @Valid orderRequest: OrderResource,
        @Header("userId") userId: String
    ): HttpResponse<String> {
        var orderResponse: OrderResponse
        return try {
            orderResponse = orderService.saveOrder(orderRequest.apply { this.userId = userId })
            val location = URI(orderResponse.paymentUrl)
            HttpResponse.redirect(location)
        } catch (ex: ValidationException) {
            HttpResponse.badRequest(ex.message)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            HttpResponse.serverError("error creating order!")
        }
    }


    @Put(value = "/order/{orderNumber}/confirm")
    fun confirmOrder(@QueryValue orderNumber: Long): HttpResponse<String> {
        try {
            orderService.updateOrderStatus(OrderStatus.PAID, orderNumber)
            val fulfillmentResponse = fulfillmentApiClient.fulfillOrder(orderNumber)
            if (fulfillmentResponse.equals("failure")) {
                return HttpResponse.serverError("Error confirming the order with order number : $orderNumber. Please try again!")
            }
            orderService.updateOrderStatus(OrderStatus.IN_FULFILLMENT, orderNumber)
            return HttpResponse.ok("Order with order number : $orderNumber is confirmed.")
        } catch (ex: NotFoundException) {
            return HttpResponse.notFound("Order with order number : $orderNumber not found!!!")
        } catch (ex: UnsupportedOperationException) {
            return HttpResponse.badRequest(ex.message)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            return HttpResponse.serverError("Error confirming order with order number : $orderNumber!!!")
        }

    }

    @Put(value = "/order/{orderNumber}/close")
    fun closeOrder(@QueryValue orderNumber: Long): HttpResponse<String> {
        try {
            orderService.updateOrderStatus(OrderStatus.CLOSED, orderNumber)
        } catch (ex: NotFoundException) {
            return HttpResponse.notFound("Order with order number : $orderNumber not found!!!")
        } catch (ex: UnsupportedOperationException) {
            return HttpResponse.badRequest(ex.message)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)
            return HttpResponse.serverError("Error closing order with order number : $orderNumber!!!")
        }
        return HttpResponse.ok("Order with order number : $orderNumber is successfully closed.")
    }

    @Get(value = "/orders")
    fun getOrdersByUser(@Header("userId") userId: String, pageable: Pageable): Page<OrderResource> {
        return orderService.getOrders(userId, pageable)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(OrderController::class.java)
    }
}
