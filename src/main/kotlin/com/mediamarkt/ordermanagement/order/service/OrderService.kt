package com.mediamarkt.ordermanagement.order.service

import com.mediamarkt.ordermanagement.order.*
import com.mediamarkt.ordermanagement.order.OrderStatus.CREATED
import com.mediamarkt.ordermanagement.order.OrderStatus.Companion.resolvedPreviousValidOrderStatuses
import com.mediamarkt.ordermanagement.order.api.OrderResource
import com.mediamarkt.ordermanagement.order.api.OrderResponse
import com.mediamarkt.ordermanagement.order.api.OrderStatusResource
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import java.time.Instant


@Singleton
open class OrderService(
    private val orderRepository: OrderRepository,
    private val orderStatusMapRepository: OrderStatusMapRepository,
    private val orderConverter: OrderConverter = OrderConverter(),
    private val orderResourceConverter: OrderResourceConverter = OrderResourceConverter(),

    ) {

    open fun saveOrder(orderRequest: OrderResource): OrderResponse {
        val savedOrder: OrderEntity?
        val order = orderConverter.convert(orderRequest)
        savedOrder = orderRepository.save(order)
        savedOrder.orderNumber?.let { orderStatusMapRepository.update(OrderStatusMap(it, CREATED)) }
        return buildOrderResponse(savedOrder, CREATED)
    }

    open fun updateOrderStatus(orderStatus: OrderStatus, orderNumber: Long): Boolean {
        val orderStatuses = orderStatusMapRepository.findByOrderNumberOrderByCreatedTimestampDesc(orderNumber)
        if (orderStatuses.isEmpty()) {
            throw NotFoundException()
        }
        if (orderStatuses[0].orderStatus == orderStatus.value) {
            orderStatusMapRepository.update(OrderStatusMap(orderNumber, orderStatus, Instant.now()))
            return true
        }
        validateOrderStatus(orderStatus, orderStatuses)
        orderStatusMapRepository.save(OrderStatusMap(orderNumber, orderStatus))
        return true
    }

    fun getOrders(userId: String, pageable: Pageable): Page<OrderResource> {
        val orderEntities = orderRepository.findByUserId(userId, pageable)
        val orderResources = orderEntities.content.map {
            orderResourceConverter.convert(it)
        }
        val orderStatusMapList =
            orderStatusMapRepository.findByOrderNumberInList(orderResources.map { it.orderNumber!! })
        orderResources.forEach { orderResource ->
            orderResource.statuses =
                orderStatusMapList.filter { orderResource.orderNumber == it.orderNumber }
                    .map { OrderStatusResource(it.orderStatus!!, it.lastModifiedTimestamp!!) }
        }
        return Page.of(orderResources, pageable, pageable.size.toLong())
    }

    companion object {
        private fun validateOrderStatus(
            orderStatus: OrderStatus,
            orderStatusesOrderedByTimestamp: List<OrderStatusMap>
        ) {
            val allPreviousValidStatuses = resolvedPreviousValidOrderStatuses(orderStatus)
            val validStatus =
                allPreviousValidStatuses.contentEquals(orderStatusesOrderedByTimestamp.map { it.orderStatus }
                    .toTypedArray())
            if (!validStatus) {
                throw UnsupportedOperationException("Order Status cannot be updated from ${orderStatusesOrderedByTimestamp[0].orderStatus} to ${orderStatus.value}!!")
            }
        }

        fun buildOrderResponse(orderEntity: OrderEntity, status: OrderStatus): OrderResponse {
            val paymentLink = "http://localhost:8080/payment/${orderEntity.orderNumber}"
            return OrderResponse(orderEntity.orderNumber, status.value, paymentLink)
        }
    }
}



