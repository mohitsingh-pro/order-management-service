package com.mediamarkt.ordermanagement.order.service

import com.mediamarkt.ordermanagement.order.ItemEntity
import com.mediamarkt.ordermanagement.order.OrderEntity
import com.mediamarkt.ordermanagement.order.api.ItemResource
import com.mediamarkt.ordermanagement.order.api.OrderResource
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton


interface Converter<S, T> {
    fun convert(source: S): T
}

@Singleton
class OrderConverter : Converter<OrderResource, OrderEntity> {
    private val itemConverter = ItemConverter()

    override fun convert(source: OrderResource) =
        OrderEntity(
            userId = source.userId,
            items = source.items.map { itemConverter.convert(it) },
            totalAmount = source.totalAmount
        )
}

@Singleton
class OrderResourceConverter : Converter<OrderEntity, OrderResource> {
    private val itemResourceConverter = ItemResourceConverter()

    override fun convert(source: OrderEntity) = OrderResource(
        orderNumber = source.orderNumber,
        items = source.items.map { itemResourceConverter.convert(it) },
        totalAmount = source.totalAmount!!
    )
}

@Introspected
class ItemResourceConverter : Converter<ItemEntity, ItemResource> {
    override fun convert(source: ItemEntity) = ItemResource(
        itemId = source.itemId!!,
        type = source.type!!,
        name = source.name!!,
        color = source.color!!,
        price = source.price!!,
        quantity = source.quantity!!
    )
}

@Introspected
class ItemConverter : Converter<ItemResource, ItemEntity> {
    override fun convert(source: ItemResource) = ItemEntity(
        itemId = source.itemId,
        type = source.type,
        name = source.name,
        color = source.color,
        price = source.price,
        quantity = source.quantity
    )
}
