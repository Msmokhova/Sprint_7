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
public class CourierTest {

    private static final String BASE_URL = "qa-scooter.praktikum-services.ru";
    private String courierId;
    private String login;
    private String password;
    private String firstName;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://" + BASE_URL;
    }

    @BeforeEach
    public void createTestData() {
        login = "testCourier" + System.currentTimeMillis();
        password = "password123";
        firstName = "Test";
    }

    @Test
    @DisplayName("Проверка успешного создания курьера")
    public void testCreateCourier() {
        createCourier(login, password, firstName)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Проверка создания дубликата курьера")
    public void testCreateDuplicateCourier() {
        createCourier(login, password, firstName)
                .then()
                .statusCode(201);

        createCourier(login, password, firstName)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Проверка создания курьера без логина")
    public void testCreateCourierWithoutLogin() {
        createCourier("", password, firstName)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Проверка создания курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        createCourier(login, "", firstName)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Проверка создания курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        createCourier(login, password, "")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));

        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Проверка создания курьера с существующим логином")
    public void testCreateCourierWithExistingLogin() {
        createCourier(login, password, firstName)
                .then()
                .statusCode(201);

        createCourier(login, "differentPassword", "DifferentName")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

        courierId = loginAndGetId(login, password);
    }

    @Step("Создание курьера с логином {login}, паролем {password} и именем {firstName}")
    private Response createCourier(String login, String password, String firstName) {
        return given()
                .header("Content-type", "application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\",\"firstName\":\"%s\"}",
                        login, password, firstName))
                .post("/api/v1/courier");
    }

    @Step("Авторизация курьера и получение ID")
    private String loginAndGetId(String login, String password) {
        return loginCourier(login, password)
                .then()
                .statusCode(200)
                .extract()
                .path("id").toString();
    }

    @Step("Авторизация курьера")
    private Response loginCourier(String login, String password) {
        return given()
                .header("Content-type", "application/json")
                .body(String.format("{\"login\":\"%s\",\"password\":\"%s\"}", login, password))
                .post("/api/v1/courier/login");
    }

    @AfterEach
    @Step("Удаление тестового курьера")
    public void tearDown() {
        if (courierId != null) {
            given()
                    .delete("/api/v1/courier/" + courierId)
                    .then()
                    .statusCode(200);
        }
    }
}