package com.example;

import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class CourierLoginTest {

    private String courierId;
    private String login;
    private String password;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @BeforeEach
    public void createTestData() {
        login = "testCourier" + System.currentTimeMillis();
        password = "password123";
    }

    @AfterEach
    public void tearDown() {
        if (courierId != null) {
            deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void testSuccessfulLogin() {
        // Создаем курьера для теста
        createCourier(login, password, "Test")
                .then()
                .statusCode(201);

        // Тестируем логин и проверяем, что возвращается id
        Response loginResponse = loginCourier(login, password)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract()
                .response();

        courierId = loginResponse.path("id").toString();
        Assertions.assertNotNull(courierId, "ID курьера не должен быть null");
        Assertions.assertTrue(courierId.matches("\\d+"), "ID курьера должен быть числом");
    }

    @Test
    @DisplayName("Авторизация с неверными учетными данными")
    public void testLoginWithWrongCredentials() {
        loginCourier("nonexistent", "wrongpassword")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация без обязательных полей")
    public void testLoginWithoutRequiredFields() {
        loginCourier("", "password")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));

        loginCourier("login", "")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    public void testLoginNonExistentCourier() {
        loginCourier("nonExistentLogin", "anyPassword")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Step("Создание курьера с логином {login}, паролем {password} и именем {firstName}")
    private Response createCourier(String login, String password, String firstName) {
        return given()
                .header("Content-type", "application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\"}",
                        login, password, firstName))
                .post("/api/v1/courier");
    }

    @Step("Авторизация курьера с логином {login} и паролем {password}")
    private Response loginCourier(String login, String password) {
        return given()
                .header("Content-type", "application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}", login, password))
                .post("/api/v1/courier/login");
    }

    @Step("Удаление курьера с ID {id}")
    private void deleteCourier(String id) {
        given()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200);
    }
}