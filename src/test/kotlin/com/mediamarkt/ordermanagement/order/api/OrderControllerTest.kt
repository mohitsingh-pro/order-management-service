package com.mediamarkt.ordermanagement.order.api

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
internal class OrderControllerTest() {


    @BeforeAll
    fun setup() {
        requestSpecification = RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setRelaxedHTTPSValidation()
            .build()
    }

    @AfterAll
    fun tearDown() {
        RestAssured.reset()
    }


    @Test
    fun saveOrder() {
        requestSpecification
            .header(Header("userId", "mock_user"))
            .body(orderRequestJson)
            .post("/order")
            .then()
            .statusCode(200)
    }

    @Test
    fun confirmOrder() {
    }

    @Test
    fun closeOrder() {
    }

    @Test
    fun getOrdersByUser() {
    }

    companion object {
        val orderRequestJson = """
            {
                "items": [
                    {
                        "itemId": "ART-101",
                        "type": "TH",
                        "name": "Samsung",
                        "color": "Blue",
                        "price": 123,
                        "quantity": 2
                    }
                ],
                "totalAmount": 324,
                "userId": "ds"
            }
        """.trimIndent()

        lateinit var requestSpecification: RequestSpecification

    }
}
