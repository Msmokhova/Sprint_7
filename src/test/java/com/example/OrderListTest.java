package com.example;

import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class OrderListTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Проверка получения списка заказов")
    public void testGetOrdersList() {
        getOrdersList()
                .then()
                .statusCode(200)
                .body("orders", not(empty()))
                .body("orders", everyItem(hasKey("id")))
                .body("orders", everyItem(hasKey("track")))
                .body("orders", everyItem(hasKey("status")))
                .body("orders.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Проверка структуры одного заказа в списке")
    public void testSingleOrderStructure() {
        getOrdersList()
                .then()
                .statusCode(200)
                .body("orders[0].id", notNullValue())
                .body("orders[0].track", notNullValue())
                .body("orders[0].courierId", anyOf(nullValue(), notNullValue()))
                .body("orders[0].firstName", notNullValue())
                .body("orders[0].lastName", notNullValue())
                .body("orders[0].address", notNullValue());
    }

    @Test
    @DisplayName("Проверка лимита")
    public void testPagination() {
        getOrdersListWithLimit(3)
                .then()
                .statusCode(200)
                .body("orders.size()", lessThanOrEqualTo(3));
    }

    @Step("Получение списка заказов")
    private Response getOrdersList() {
        return given()
                .get("/api/v1/orders");
    }

    @Step("Получение списка заказов с лимитом {limit}")
    private Response getOrdersListWithLimit(int limit) {
        return given()
                .queryParam("limit", limit)
                .get("/api/v1/orders");
    }
}