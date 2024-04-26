package pages;

import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class Address {

    private String baseUrl;

    public Address(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public double getBalance(String label, String apiKey) {
        double balance = 0;
        Response response = given()
            .param("api_key", apiKey)
            .param("label", label)
            .when()
            .get(baseUrl + "/get_balance/");
        if (response.getStatusCode() == 200) {
            balance = Double.parseDouble(response.getBody().jsonPath().getString("data.available_balance"));
        }
        return balance;
    }

    public void sendTestTransaction(String apiKey, String amount, String fromAddress, String toAddress) {
        given()
            .param("api_key", apiKey)
            .param("priority", "custom")
            .param("custom_network_fee", "0.00002")
            .param("amounts", amount)
            .param("from_addresses", fromAddress)
            .param("to_addresses", toAddress)
            .when()
                .post(baseUrl + "/prepare_transaction/")
            .then()
                .assertThat()
                    .statusCode(200);
    }
}
