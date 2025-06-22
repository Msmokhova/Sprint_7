package com.example.tests;
import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class OrderListTest extends BaseTest {

    @Test
    @DisplayName("Проверка получения списка заказов")
    public void testGetOrdersList() {
        Response response = getOrdersList();
        verifyOrdersListNotEmpty(response);
        verifyOrdersListStructure(response);
    }

    @Test
    @DisplayName("Проверка структуры одного заказа в списке")
    public void testSingleOrderStructure() {
        Response response = getOrdersList();
        verifySingleOrderStructure(response);
    }

    @Test
    @DisplayName("Проверка лимита")
    public void testPagination() {
        int limit = 3;
        Response response = getOrdersListWithLimit(limit);
        verifyOrdersListLimit(response, limit);
    }

    @Step("Получение списка заказов")
    private Response getOrdersList() {
        return orderApi.getOrdersList();
    }

    @Step("Проверка, что список заказов не пуст")
    private void verifyOrdersListNotEmpty(Response response) {
        response.then()
                .body("orders", not(empty()))
                .body("orders.size()", greaterThan(0));
    }

    @Step("Проверка структуры списка заказов")
    private void verifyOrdersListStructure(Response response) {
        response.then()
                .body("orders", everyItem(hasKey("id")))
                .body("orders", everyItem(hasKey("track")))
                .body("orders", everyItem(hasKey("status")));
    }

    @Step("Проверка структуры одного заказа")
    private void verifySingleOrderStructure(Response response) {
        response.then()
                .body("orders[0].id", notNullValue())
                .body("orders[0].track", notNullValue())
                .body("orders[0].courierId", anyOf(nullValue(), notNullValue()))
                .body("orders[0].firstName", notNullValue())
                .body("orders[0].lastName", notNullValue())
                .body("orders[0].address", notNullValue());
    }

    @Step("Получение списка заказов с лимитом {limit}")
    private Response getOrdersListWithLimit(int limit) {
        return orderApi.getOrdersListWithLimit(limit);
    }

    @Step("Проверка ограничения количества заказов")
    private void verifyOrdersListLimit(Response response, int limit) {
        response.then()
                .body("orders.size()", lessThanOrEqualTo(limit));
    }
}