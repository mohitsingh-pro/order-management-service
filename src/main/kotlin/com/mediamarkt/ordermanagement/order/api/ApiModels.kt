package com.mediamarkt.ordermanagement.order.api

import com.fasterxml.jackson.annotation.JsonCreator
import io.micronaut.core.annotation.Introspected
import java.time.Instant
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@Introspected
data class OrderResource(
    val items: List<ItemResource> = listOf(),
    @PositiveOrZero
    val totalAmount: Int,
    var orderNumber: Long? = null,
    var statuses: List<OrderStatusResource>? = null,
    var userId: String? = null,
) {
    @JsonCreator
    constructor(
        items: List<ItemResource>,
        totalAmount: Int,
        orderNumber: Long? = null,
        statuses: List<OrderStatusResource>? = null
    ) : this(
        items,
        totalAmount,
        orderNumber,
        statuses,
        null
    )
}

data class OrderStatusResource(val status: String, val lastModifiedTimestamp: Instant)


data class ItemResource(
    @NotNull
    @NotBlank(message = "Id of the Item cannot not be empty/blank.")
    val itemId: String,
    @NotNull
    @NotBlank(message = "Type of the Item cannot not be empty/blank.")
    val type: String,
    @NotBlank(message = "Name of the Item cannot not be empty/blank.")
    val name: String,
    @NotBlank(message = "Color of the Item cannot not be empty/blank.")
    val color: String,
    @PositiveOrZero(message = "Price of the Item cannot be negative.")
    val price: Int,
    @Positive(message = "Price of the Item cannot be zero or negative.")
    val quantity: Int
)
