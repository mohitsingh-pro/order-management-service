package com.mediamarkt.ordermanagement.order

import com.mediamarkt.ordermanagement.order.OrderStatus.*
import com.mediamarkt.ordermanagement.order.api.ItemResource
import com.mediamarkt.ordermanagement.order.api.OrderResource
import com.mediamarkt.ordermanagement.order.service.OrderService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer


@MicronautTest
class OrderServiceTest(
    private val orderRepository: OrderRepository,
    private val itemRepository: ItemRepository,
    private val orderStatusMapRepository: OrderStatusMapRepository,
    private val orderService: OrderService
) {

    @BeforeEach
    fun cleanTables() {
        orderRepository.deleteAll()
        itemRepository.deleteAll()
        orderStatusMapRepository.deleteAll()
    }

    @Test
    fun `should save order with items`() {
        val expectedItems = listOf(itemMobile, itemEarphone)
        val order = orderService.saveOrder(OrderResource(items = expectedItems, totalAmount = 450))
        val orderEntity = orderRepository.findById(order.orderNumber).get()
        val actualItems = itemRepository.findAll()
        val orderStatusMap = orderStatusMapRepository.findAll()
        assertThat(orderEntity).isNotNull
        assertThat(orderEntity.totalAmount).isEqualTo(450)
        assertThat(actualItems.size).isEqualTo(2)
        assertItems(actualItems, expectedItems)
        assertThat(orderStatusMap.size).isEqualTo(1)
        assertThat(orderStatusMap[0].orderStatus).isEqualTo(CREATED.value)
        assertThat(orderStatusMap[0].orderNumber).isEqualTo(order.orderNumber)
    }

    @Test
    fun `should change status of the order`() {
        val expectedItems = listOf(itemMobile, itemEarphone)
        val orderNumber = orderService.saveOrder(OrderResource(items = expectedItems, totalAmount = 450)).orderNumber!!
        val orderStatusMapCreated = orderStatusMapRepository.findAll()
        assertThat(orderStatusMapCreated[0].orderStatus).isEqualTo(CREATED.value)
        assertThat(orderStatusMapCreated[0].orderNumber).isEqualTo(orderNumber)

        var updated = orderService.updateOrderStatus(PAID, orderNumber)
        assertThat(updated).isEqualTo(true)
        val orderStatusMapAfterPaid = orderStatusMapRepository.findAll()
        assertThat(orderStatusMapAfterPaid.size).isEqualTo(2)
        val orderStatusMapAfterPaidSorted = orderStatusMapAfterPaid.sortedByDescending { it.createdTimestamp }
        assertThat(orderStatusMapAfterPaidSorted[0].orderStatus).isEqualTo(PAID.value)
        assertThat(orderStatusMapAfterPaidSorted[0].orderNumber).isEqualTo(orderNumber)
        assertThat(orderStatusMapAfterPaidSorted[1].orderStatus).isEqualTo(CREATED.value)
        assertThat(orderStatusMapAfterPaidSorted[1].orderNumber).isEqualTo(orderNumber)


        updated = orderService.updateOrderStatus(IN_FULFILLMENT, orderNumber)
        assertThat(updated).isEqualTo(true)
        val orderStatusAfterInFulfillment = orderStatusMapRepository.findAll()
        assertThat(orderStatusAfterInFulfillment.size).isEqualTo(3)
        val orderStatusAfterInFulfillmentSorted =
            orderStatusAfterInFulfillment.sortedByDescending { it.createdTimestamp }
        assertThat(orderStatusAfterInFulfillmentSorted[0].orderStatus).isEqualTo(IN_FULFILLMENT.value)
        assertThat(orderStatusAfterInFulfillmentSorted[0].orderNumber).isEqualTo(orderNumber)
        assertThat(orderStatusAfterInFulfillmentSorted[1].orderStatus).isEqualTo(PAID.value)
        assertThat(orderStatusAfterInFulfillmentSorted[1].orderNumber).isEqualTo(orderNumber)
        assertThat(orderStatusAfterInFulfillmentSorted[2].orderStatus).isEqualTo(CREATED.value)
        assertThat(orderStatusAfterInFulfillmentSorted[2].orderNumber).isEqualTo(orderNumber)


        updated = orderService.updateOrderStatus(CLOSED, orderNumber)
        assertThat(updated).isEqualTo(true)
        val orderStatusAfterClosed = orderStatusMapRepository.findAll()
        assertThat(orderStatusAfterClosed.size).isEqualTo(4)
        val orderStatusAfterClosedSorted = orderStatusAfterClosed.sortedByDescending { it.createdTimestamp }
        assertThat(orderStatusAfterClosedSorted[0].orderStatus).isEqualTo(CLOSED.value)
        assertThat(orderStatusAfterClosedSorted[0].orderNumber).isEqualTo(orderNumber)
        assertThat(orderStatusAfterClosedSorted[1].orderStatus).isEqualTo(IN_FULFILLMENT.value)
        assertThat(orderStatusAfterClosedSorted[1].orderNumber).isEqualTo(orderNumber)
        assertThat(orderStatusAfterClosedSorted[2].orderStatus).isEqualTo(PAID.value)
        assertThat(orderStatusAfterClosedSorted[2].orderNumber).isEqualTo(orderNumber)
        assertThat(orderStatusAfterClosedSorted[3].orderStatus).isEqualTo(CREATED.value)
        assertThat(orderStatusAfterClosedSorted[3].orderNumber).isEqualTo(orderNumber)
    }

    @Test
    fun `should throw exception for status change from CREATED to CLOSED`() {
        val expectedItems = listOf(itemMobile, itemEarphone)
        val orderNumber = orderService.saveOrder(OrderResource(items = expectedItems, totalAmount = 450)).orderNumber!!
        val orderStatusMapCreated = orderStatusMapRepository.findAll()
        assertThat(orderStatusMapCreated[0].orderStatus).isEqualTo(CREATED.value)
        assertThat(orderStatusMapCreated[0].orderNumber).isEqualTo(orderNumber)
        assertThrows<UnsupportedOperationException> { orderService.updateOrderStatus(CLOSED, orderNumber) }
    }


    private fun assertItems(
        expectedItems: List<ItemEntity>,
        actualItems: List<ItemResource>,
    ) {
        assertThat(actualItems.map { it.itemId }).containsAll(expectedItems.map { it.itemId })
        assertThat(actualItems.map { it.type }).containsAll(expectedItems.map { it.type })
        assertThat(actualItems.map { it.name }).containsAll(expectedItems.map { it.name })
        assertThat(actualItems.map { it.color }).containsAll(expectedItems.map { it.color })
        assertThat(actualItems.map { it.price }).containsAll(expectedItems.map { it.price })
        assertThat(actualItems.map { it.quantity }).containsAll(expectedItems.map { it.quantity })
    }

    companion object {
        val itemMobile = ItemResource("ITEM-1", "Mobile", "Samsung", "Yellow", 430, 1)
        val itemEarphone = ItemResource("ITEM-1", "Mobile", "Samsung", "Yellow", 10, 2)
        var postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:alpine3.16")

        @BeforeAll
        fun startContainer() {
            postgreSQLContainer.start()
        }

        @AfterAll
        fun stopContainer() {
            postgreSQLContainer.stop()
        }
    }

}
