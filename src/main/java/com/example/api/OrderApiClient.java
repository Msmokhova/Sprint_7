package com.example.api;

import com.example.models.Order;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderApiClient extends BaseApiClient {

    public Response createOrder(Order order) {
        return post("/api/v1/orders", order);
    }

    public Response getOrdersList() {
        return get("/api/v1/orders");
    }

    public Response getOrdersListWithLimit(int limit) {
        return given()
                .queryParam("limit", limit)
                .get("/api/v1/orders");
    }
}