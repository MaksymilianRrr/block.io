package tests;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.Address;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AddressTestsHttpClient {

    String baseUrl = "https://block.io/api/v2";
    String apiKey = "ad34-6664-54e0-b670";
    String label = "test-label-client-b";

    @Test(priority = -1)
    public void testCreateAddressWithLabel() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(baseUrl + "/get_new_address/");
            request.addHeader("Content-Type", "application/json");

            StringEntity params = new StringEntity("{\"api_key\":\"" + apiKey + "\",\"label\":\"" + label + "\"}");
            request.setEntity(params);

            HttpResponse response = httpClient.execute(request);

            assertThat("Status code is not 200", response.getStatusLine().getStatusCode(), equalTo(200));

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            assertThat("Label is not equal", responseBody, containsString("\"label\": \"" + label + "\""));
        }
    }


    @DataProvider(name = "testData")
    public Object[][] testData() {
        return new Object[][]{
                {"default", "0.00011727"},
                {label, "0.00000000"},
        };
    }

    @Test(dataProvider = "testData")
    public void testBalance(String label, String value) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(baseUrl + "/get_balance/");
            request.addHeader("Content-Type", "application/json");

            StringEntity params = new StringEntity("{\"api_key\":\"" + apiKey + "\",\"label\":\"" + label + "\"}");
            request.setEntity(params);

            HttpResponse response = httpClient.execute(request);

            assertThat("Status code is not 200", response.getStatusLine().getStatusCode(), equalTo(200));

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            assertThat("Label is not equal", responseBody, containsString("\"label\": \"" + label + "\""));
            assertThat("Available balance is not equal", responseBody, containsString("\"available_balance\": \"" + value + "\""));
        }
    }

    @Test
    public void testBalanceAfterTransaction() throws IOException {
        Address account = new Address(baseUrl);

        double initialBalance = 0.00011727;
        double expectedBalance = initialBalance - 0.00004;

        double finalBalance = account.getBalance("default", apiKey);

        assertThat("Balance is not updated correctly after transaction", finalBalance, equalTo(expectedBalance));
    }

    @Test(dependsOnMethods = "testBalanceAfterTransaction")
    public void testRecentTransactions() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequest request = new HttpGet(baseUrl + "/get_transactions/?api_key=" + apiKey + "&type=sent");

            HttpResponse response = httpClient.execute(request);

            assertThat("Status code is not 200", response.getStatusLine().getStatusCode(), equalTo(200));

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            assertThat("Status is not success", responseBody, containsString("\"status\": \"success\""));
            assertThat("Transactions are empty", responseBody, not(containsString("\"txs\": []")));
        }
    }
}
