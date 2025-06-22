package com.example.tests;

import com.example.models.Order;
import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderTest extends BaseTest {
    private static final Order DEFAULT_ORDER = new Order(
            "Мария",
            "Мохова",
            "ул. Пупкина, д. 23",
            4,
            "+7 9001234567",
            5,
            "2025-07-06",
            "Тестовый заказ",
            Collections.emptyList()
    );

    private static Stream<Arguments> provideColorData() {
        return Stream.of(
                Arguments.of(Arrays.asList("BLACK")),
                Arguments.of(Arrays.asList("GREY")),
                Arguments.of(Arrays.asList("BLACK", "GREY")),
                Arguments.of(Collections.emptyList())
        );
    }

    @ParameterizedTest
    @MethodSource("provideColorData")
    @DisplayName("Создание заказа с разными цветами")
    public void testCreateOrderWithDifferentColors(List<String> colors) {
        Order order = createOrderWithColors(colors);
        Response response = createOrder(order);
        verifyOrderCreation(response);
        verifyOrderData(response, order);
    }

    @Test
    @DisplayName("Получение списка заказов")
    public void testGetOrdersList() {
        createTestOrder();
        Response response = getOrdersList();
        verifyOrdersList(response);
    }

    @Test
    @DisplayName("Получение списка заказов с лимитом")
    public void testGetOrdersListWithLimit() {
        createTestOrder();
        Response response = getOrdersListWithLimit(1);
        verifyOrdersListWithLimit(response, 1);
    }

    @Step("Создание тестового заказа")
    private void createTestOrder() {
        Order order = createOrderWithColors(Collections.emptyList());
        createOrder(order);
    }

    @Step("Создание заказа с цветами: {colors}")
    private Order createOrderWithColors(List<String> colors) {
        return new Order(
                DEFAULT_ORDER.getFirstName(),
                DEFAULT_ORDER.getLastName(),
                DEFAULT_ORDER.getAddress(),
                DEFAULT_ORDER.getMetroStation(),
                DEFAULT_ORDER.getPhone(),
                DEFAULT_ORDER.getRentTime(),
                DEFAULT_ORDER.getDeliveryDate(),
                DEFAULT_ORDER.getComment(),
                colors
        );
    }

    @Step("Отправка запроса на создание заказа")
    private Response createOrder(Order order) {
        return orderApi.createOrder(order)
                .then()
                .extract()
                .response();
    }

    @Step("Проверка создания заказа")
    private void verifyOrderCreation(Response response) {
        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Step("Проверка данных заказа")
    private void verifyOrderData(Response response, Order expectedOrder) {
        int trackId = response.jsonPath().getInt("track");
        orderApi.getOrderByTrack(trackId)
                .then()
                .statusCode(200)
                .body("order.firstName", equalTo(expectedOrder.getFirstName()))
                .body("order.lastName", equalTo(expectedOrder.getLastName()))
                .body("order.address", equalTo(expectedOrder.getAddress()));
    }

    @Step("Получение списка заказов")
    private Response getOrdersList() {
        return orderApi.getOrdersList()
                .then()
                .extract()
                .response();
    }

    @Step("Проверка списка заказов")
    private void verifyOrdersList(Response response) {
        response.then()
                .statusCode(200)
                .body("orders", not(empty()))
                .body("orders[0].id", notNullValue());
    }

    @Step("Получение списка с лимитом {limit}")
    private Response getOrdersListWithLimit(int limit) {
        return orderApi.getOrdersListWithLimit(limit)
                .then()
                .extract()
                .response();
    }

    @Step("Проверка лимита заказов")
    private void verifyOrdersListWithLimit(Response response, int limit) {
        response.then()
                .statusCode(200)
                .body("orders.size()", lessThanOrEqualTo(limit));
    }
}