package com.example.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class BaseApiClient {
    static {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    protected Response post(String path, Object body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .post(path);
    }

    protected Response get(String path) {
        return given().get(path);
    }

    protected Response delete(String path) {
        return given().delete(path);
    }
}
