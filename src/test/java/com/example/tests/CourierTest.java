package com.example.tests;

import com.example.models.Courier;
import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class CourierTest extends BaseTest {
    private String courierId;
    private String login;
    private String password;
    private final String firstName = "Test";

    @BeforeEach
    public void createTestData() {
        login = "testCourier" + System.currentTimeMillis();
        password = "password123";
    }

    @Test
    @DisplayName("Успешное создание курьера")
    public void testSuccessfulCourierCreation() {
        Courier courier = createCourier(login, password, firstName);
        Response response = createCourierRequest(courier);
        verifySuccessfulCreation(response);
        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Проверка создания дубликата курьера")
    public void testCreateDuplicateCourier() {
        Courier courier = createCourier(login, password, firstName);
        createCourierRequest(courier);
        attemptDuplicateCreation(login, password, firstName);
        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Проверка создания курьера без логина")
    public void testCreateCourierWithoutLogin() {
        createAndVerifyCourier("", password, firstName, 400);
    }

    @Test
    @DisplayName("Проверка создания курьера без пароля")
    public void testCreateCourierWithoutPassword() {
        createAndVerifyCourier(login, "", firstName, 400);
    }

    @Test
    @DisplayName("Проверка создания курьера без имени")
    public void testCreateCourierWithoutFirstName() {
        Courier courier = createCourier(login, password, "");
        verifySuccessfulCreation(courier);
        courierId = loginAndGetId(login, password);
    }

    @Test
    @DisplayName("Проверка создания курьера с существующим логином")
    public void testCreateCourierWithExistingLogin() {
        createAndVerifyCourier(login, password, firstName, 201);
        attemptCreateWithDifferentData(login, "differentPassword", "DifferentName");
        courierId = loginAndGetId(login, password);
    }

    @AfterEach
    public void tearDown() {
        if (courierId != null) {
            deleteCourier(courierId);
        }
    }

    @Step("Создание объекта курьера (логин: {login})")
    private Courier createCourier(String login, String password, String firstName) {
        return new Courier(login, password, firstName);
    }

    @Step("Отправка запроса на создание курьера")
    private Response createCourierRequest(Courier courier) {
        return courierApi.createCourier(courier);
    }

    @Step("Проверка успешного создания курьера")
    private void verifySuccessfulCreation(Response response) {
        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Проверка успешного создания курьера (перегрузка для объекта)")
    private void verifySuccessfulCreation(Courier courier) {
        courierApi.createCourier(courier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Создание и проверка курьера (ожидаемый статус: {expectedStatus})")
    private void createAndVerifyCourier(String login, String password, String firstName, int expectedStatus) {
        courierApi.createCourier(new Courier(login, password, firstName))
                .then()
                .statusCode(expectedStatus);
    }

    @Step("Попытка создания дубликата курьера")
    private void attemptDuplicateCreation(String login, String password, String firstName) {
        courierApi.createCourier(new Courier(login, password, firstName))
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Step("Попытка создания с существующим логином и другими данными")
    private void attemptCreateWithDifferentData(String login, String password, String firstName) {
        courierApi.createCourier(new Courier(login, password, firstName))
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Step("Авторизация и получение ID курьера")
    private String loginAndGetId(String login, String password) {
        return courierApi.loginAndGetId(login, password);
    }

    @Step("Удаление курьера (ID: {courierId})")
    private void deleteCourier(String courierId) {
        courierApi.deleteCourier(courierId)
                .then()
                .statusCode(200);
    }
}