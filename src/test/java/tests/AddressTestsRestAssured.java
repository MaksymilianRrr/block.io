package tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.Address;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AddressTestsRestAssured {

    String baseUrl = "https://block.io/api/v2";
    String apiKey = "ad34-6664-54e0-b670";
    String label = "test-label";

    @Test(priority = -1)
    public void testCreateAddressWithLabel() {
        given()
                .param("api_key", apiKey)
                .param("label", label)
                .when()
                .post(baseUrl + "/get_new_address/")
                .then()
                .assertThat()
                .statusCode(200)
                .assertThat()
                .body("data.address", not(empty()))
                .body("data.label", equalTo(label));
    }

    @DataProvider(name = "testData")
    public Object[][] testData() {
        return new Object[][]{
                {"default", "0.00011727"},
                {label, "0.00000000"},
        };
    }

    @Test(dataProvider = "testData")
    public void testBalance(String label, String value) {
        given()
                .param("api_key", apiKey)
                .param("label", label)
                .when()
                .post(baseUrl + "/get_balance/")
                .then()
                .assertThat()
                .statusCode(200)
                .assertThat()
                .body("data.balances[0].label", equalTo(label))
                .body("data.available_balance", not(empty()))
                .body("data.available_balance", equalTo(value));
    }

    @Test
    public void testBalanceAfterTransaction() {
        Address account = new Address(baseUrl);

        // double initialBalance = 0.00011727; this is for testing
        double initialBalance = account.getBalance("default", apiKey);
        double expectedBalance = initialBalance - 0.00004;
        // send money here testAccount.sendTestTransaction(...);
        double finalBalance = account.getBalance("default", apiKey);

        assertThat("Balance is not updated correctly after transaction", finalBalance, equalTo(expectedBalance));
    }

    @Test(dependsOnMethods = "testBalanceAfterTransaction")
    public void testRecentTransactions() {
        given()
                .param("api_key", apiKey)
                .param("type", "sent")
                .when()
                .get(baseUrl + "/get_transactions/")
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.txs", not(empty()))
                .body("data.txs[0].total_amount_sent", notNullValue());
    }
}
