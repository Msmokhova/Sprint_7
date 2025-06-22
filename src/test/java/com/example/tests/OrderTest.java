package com.example.tests;

import com.example.api.OrderApiClient;
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
                Arguments.of("Один цвет: BLACK", Arrays.asList("BLACK")),
                Arguments.of("Один цвет: GREY", Arrays.asList("GREY")),
                Arguments.of("Два цвета", Arrays.asList("BLACK", "GREY")),
                Arguments.of("Без цвета", Collections.emptyList())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideColorData")
    @DisplayName("Создание заказа с разными цветами")
    public void testCreateOrderWithDifferentColors(String testName, List<String> colors) {
        Order order = createTestOrder(colors);
        Response response = createOrder(order);
        verifyOrderCreation(response);
    }

    @Test
    @DisplayName("Получение списка заказов")
    public void testGetOrdersList() {
        Response response = getOrdersList();
        verifyOrdersList(response);
    }

    @Test
    @DisplayName("Получение списка заказов с лимитом")
    public void testGetOrdersListWithLimit() {
        int limit = 1;
        Response response = getOrdersListWithLimit(limit);
        verifyOrdersListWithLimit(response, limit);
    }

    @Step("Создание тестового заказа с цветами: {colors}")
    private Order createTestOrder(List<String> colors) {
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
        return orderApi.createOrder(order);
    }

    @Step("Проверка успешного создания заказа")
    private void verifyOrderCreation(Response response) {
        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Step("Получение списка заказов")
    private Response getOrdersList() {
        return orderApi.getOrdersList();
    }

    @Step("Проверка списка заказов")
    private void verifyOrdersList(Response response) {
        response.then()
                .statusCode(200)
                .body("orders", notNullValue());
    }

    @Step("Получение списка заказов с лимитом {limit}")
    private Response getOrdersListWithLimit(int limit) {
        return orderApi.getOrdersListWithLimit(limit);
    }

    @Step("Проверка списка заказов с лимитом")
    private void verifyOrdersListWithLimit(Response response, int limit) {
        response.then()
                .statusCode(200)
                .body("orders.size()", lessThanOrEqualTo(limit))
                .body("orders", not(empty()));
    }
}