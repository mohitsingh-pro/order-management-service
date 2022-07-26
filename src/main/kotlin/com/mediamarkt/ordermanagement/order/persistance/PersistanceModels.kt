package com.mediamarkt.ordermanagement.order

import java.time.Instant
import javax.persistence.*

@Table(name = "orders")
@Entity
data class OrderEntity @JvmOverloads constructor(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_number_generator")
    @SequenceGenerator(name = "order_number_generator", sequenceName = "order_number_generator")
    @Column(name = "order_number", updatable = false, nullable = false)
    var orderNumber: Long? = null,
    @Column(name = "user_id")
    val userId: String? = null,
    @OneToMany(mappedBy = "id", cascade = [CascadeType.ALL])
    val items: List<ItemEntity> = listOf(),
    @Column(name = "total_amount")
    val totalAmount: Int? = null,
    @Column(name = "created_timestamp")
    val createdTimestamp: Instant = Instant.now()
)

@Table(name = "items")
@Entity
data class ItemEntity @JvmOverloads constructor(
    @Id @GeneratedValue(
        strategy = GenerationType.SEQUENCE, generator = "item_id_generator"
    ) @SequenceGenerator(name = "item_id_generator", sequenceName = "item_id_generator") @Column(
        name = "id", updatable = false, nullable = false
    ) var id: Long? = null,
    var itemId: String? = null,
    val type: String? = null,
    val name: String? = null,
    val color: String? = null,
    val price: Int? = null,
    val userId: String? = null,
    val quantity: Int? = null,
    @JoinColumn(name = "orderNumber")
    @ManyToOne(cascade = [CascadeType.ALL])
    val order: OrderEntity? = null,
)

@Table(name = "order_status")
@Entity
data class OrderStatusMap(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_status_map_id_generator")
    @SequenceGenerator(name = "order_status_map_id_generator", sequenceName = "order_status_map_id_generator")
    @Column(name = "id", updatable = false, nullable = false)
    var id: Long? = null,
    @Column(name = "orderNumber", nullable = false)
    val orderNumber: Long? = null,
    @Column(name = "order_status")
    val orderStatus: String? = null,
    @Column(name = "created_timestamp")
    val createdTimestamp: Instant = Instant.now(),
    @Column(name = "last_modified_timestamp")
    val lastModifiedTimestamp: Instant? = Instant.now()
) {
    constructor(orderNumber: Long?, orderStatus: OrderStatus, lastModifiedTimestamp: Instant) : this(
        null,
        orderNumber,
        orderStatus.value,
        lastModifiedTimestamp
    )

    constructor(orderNumber: Long?, orderStatus: OrderStatus) : this(
        null,
        orderNumber,
        orderStatus.value
    )
}

enum class OrderStatus(val order: Int, val value: String) {
    CREATED(1, "Created"), PAID(2, "Paid"), IN_FULFILLMENT(3, "In Fulfillment"), CLOSED(4, "Closed");

    companion object {
        fun resolvedPreviousValidOrderStatuses(orderStatus: OrderStatus) =
            when (orderStatus.order) {
                2 -> arrayOf(CREATED.value)
                3 -> arrayOf(PAID.value, CREATED.value)
                4 -> arrayOf(IN_FULFILLMENT.value, PAID.value, CREATED.value)
                else -> arrayOf<OrderStatus>()
            }
    }
}

