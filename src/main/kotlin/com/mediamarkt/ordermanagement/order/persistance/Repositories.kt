package com.mediamarkt.ordermanagement.order

import io.micronaut.data.annotation.Repository
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.data.repository.CrudRepository
import javax.transaction.Transactional

@Transactional
@Repository
interface OrderRepository : CrudRepository<OrderEntity?, Long?> {
    fun findByUserId(userId: String, pageable: Pageable): Page<OrderEntity>
}

@Transactional
@Repository
interface ItemRepository : CrudRepository<ItemEntity?, Long?> {
    override fun findAll(): List<ItemEntity>
}

@Transactional
@Repository
interface OrderStatusMapRepository : CrudRepository<OrderStatusMap?, Long?> {
    override fun findAll(): List<OrderStatusMap>
    fun findByOrderNumberOrderByCreatedTimestampDesc(orderNumber: Long): List<OrderStatusMap>
    fun findByOrderNumberInList(orderNumber: List<Long>): List<OrderStatusMap>
}
