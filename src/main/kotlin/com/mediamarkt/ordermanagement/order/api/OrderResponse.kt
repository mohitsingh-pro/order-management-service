package com.mediamarkt.ordermanagement.order.api

data class OrderResponse(val orderNumber: Long?, val status: String?, val paymentUrl: String)
