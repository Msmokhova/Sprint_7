package com.example.api;

import com.example.models.Courier;
import io.restassured.response.Response;

public class CourierApiClient extends BaseApiClient {

    public Response createCourier(Courier courier) {
        return post("/api/v1/courier", courier);
    }

    public Response loginCourier(String login, String password) {
        return post("/api/v1/courier/login", new LoginCredentials(login, password));
    }

    public Response deleteCourier(String id) {
        return delete("/api/v1/courier/" + id);
    }

    public String loginAndGetId(String login, String password) {
        return loginCourier(login, password)
                .then()
                .extract()
                .path("id")
                .toString();
    }

    private static class LoginCredentials {
        private final String login;
        private final String password;

        public LoginCredentials(String login, String password) {
            this.login = login;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }
}