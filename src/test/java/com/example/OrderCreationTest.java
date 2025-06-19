package com.example;

import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.Arrays;
import java.util.stream.Stream;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AllureJunit5.class)
public class OrderCreationTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    private static Stream<Arguments> provideColorData() {
        return Stream.of(
                Arguments.of("BLACK"),
                Arguments.of("GREY"),
                Arguments.of("BLACK,GREY"),
                Arguments.of("")
        );
    }

    @ParameterizedTest(name = "Создание заказа с цветами: {0}")
    @MethodSource("provideColorData")
    @DisplayName("Проверка создания заказа с разными вариантами цветов")
    public void testCreateOrderWithDifferentColors(String colorParam) {
        String[] colors = colorParam.isEmpty() ? new String[0] : colorParam.split(",");

        Response response = createOrder(colors)
                .then()
                .statusCode(201)
                .body("track", notNullValue())
                .body("$", hasKey("track"))
                .extract().response();

        int trackNumber = response.path("track");
        assertTrue(trackNumber > 0, "Track number должен быть положительным числом");
    }

    @Step("Создание заказа с цветами: {colors}")
    private Response createOrder(String[] colors) {
        String colorJson = colors.length > 0 ? "\"color\": " + Arrays.toString(colors).replace("[", "[\"").replace("]", "\"]").replace(", ", "\", \"") + "," : "";
        String requestBody = String.format("{" +
                "\"firstName\": \"Мария\"," +
                "\"lastName\": \"Мохова\"," +
                "\"address\": \"ул. Пупкина, д. 23\"," +
                "\"metroStation\": 4," +
                "\"phone\": \"+7 9001234567\"," +
                "\"rentTime\": 5," +
                "\"deliveryDate\": \"2025-07-06\"," +
                "\"comment\": \"Тестовый заказ\"," +
                "%s" +
                "}", colorJson).replace(",}", "}");
        return given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .post("/api/v1/orders");
    }
}