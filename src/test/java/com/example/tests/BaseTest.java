package com.example.tests;

import com.example.api.CourierApiClient;
import com.example.api.OrderApiClient;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AllureJunit5.class)
public abstract class BaseTest {
    protected static CourierApiClient courierApi;
    protected static OrderApiClient orderApi;  // Добавляем OrderApiClient

    @BeforeAll
    public static void setup() {
        courierApi = new CourierApiClient();
        orderApi = new OrderApiClient();  // Инициализируем OrderApiClient
    }
}