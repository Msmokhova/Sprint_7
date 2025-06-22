package com.example.tests;

import com.example.models.Courier;
import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class CourierLoginTest extends BaseTest {
    private String courierId;
    private String login;
    private String password;

    @BeforeEach
    public void setUp() {
        login = "testCourier" + System.currentTimeMillis();
        password = "password123";
        courierId = createTestCourier(login, password);
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void testSuccessfulLogin() {
        Response response = loginCourier(login, password);
        verifySuccessfulLogin(response);
    }

    @Test
    @DisplayName("Авторизация с неверными учетными данными")
    public void testLoginWithWrongCredentials() {
        Response response = loginCourier(login, "wrongpassword");
        verifyFailedLogin(response, 404, "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Авторизация без логина")
    public void testLoginWithoutLogin() {
        Response response = loginCourier("", password);
        verifyFailedLogin(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация без пароля")
    public void testLoginWithoutPassword() {
        Response response = loginCourier(login, "");
        verifyFailedLogin(response, 400, "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Авторизация несуществующего курьера")
    public void testLoginNonExistentCourier() {
        String nonExistentLogin = "nonExistent" + System.currentTimeMillis();
        Response response = loginCourier(nonExistentLogin, "anyPassword");
        verifyFailedLogin(response, 404, "Учетная запись не найдена");
    }

    @Step("Создание тестового курьера")
    private String createTestCourier(String login, String password) {
        Courier courier = new Courier(login, password, "Test");
        courierApi.createCourier(courier)
                .then()
                .statusCode(201);
        return courierApi.loginAndGetId(login, password);
    }

    @Step("Авторизация курьера (логин: {login})")
    private Response loginCourier(String login, String password) {
        return courierApi.loginCourier(login, password);
    }

    @Step("Проверка успешной авторизации")
    private void verifySuccessfulLogin(Response response) {
        response.then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Step("Проверка неудачной авторизации (ожидаемый статус: {expectedStatus})")
    private void verifyFailedLogin(Response response, int expectedStatus, String expectedMessage) {
        response.then()
                .statusCode(expectedStatus)
                .body("message", equalTo(expectedMessage));
    }

    @Step("Удаление курьера (ID: {courierId})")
    private void deleteCourier(String courierId) {
        courierApi.deleteCourier(courierId)
                .then()
                .statusCode(200);
    }

    @AfterEach
    public void tearDown() {
        if (courierId != null) {
            deleteCourier(courierId);
        }
    }
}