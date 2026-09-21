package com.dmfs.payment.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PesaPalClient {

    private final RestClient restClient;

    private final String consumerKey;
    private final String consumerSecret;
    private final String callbackUrl;
    private final String ipnNotificationId;
    private final String currency;

    public PesaPalClient(
            @Value("${pesapal.base-url}") String baseUrl,
            @Value("${pesapal.consumer-key:}") String consumerKey,
            @Value("${pesapal.consumer-secret:}") String consumerSecret,
            @Value("${pesapal.callback-url}") String callbackUrl,
            @Value("${pesapal.ipn-notification-id:}") String ipnNotificationId,
            @Value("${pesapal.currency:TZS}") String currency
    ) {

        this.restClient =
                RestClient
                        .builder()
                        .baseUrl(baseUrl)
                        .build();

        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.callbackUrl = callbackUrl;
        this.ipnNotificationId = ipnNotificationId;
        this.currency = currency;
    }

    public String submitOrder(
            String merchantReference,
            BigDecimal amount,
            String description,
            String email,
            String firstName,
            String lastName
    ) {

        validateConfiguration();

        String token = requestToken();

        if (ipnNotificationId == null
                || ipnNotificationId.isBlank()) {

            throw new RuntimeException(
                    "PesaPal IPN notification ID is not configured"
            );
        }

        Map<String, Object> billing =
                new LinkedHashMap<>();

        billing.put("email_address", email);
        billing.put("country_code", "TZ");
        billing.put("first_name", firstName);
        billing.put("last_name", lastName);

        Map<String, Object> order =
                new LinkedHashMap<>();

        order.put("id", merchantReference);
        order.put("currency", currency);
        order.put("amount", amount);
        order.put(
                "description",
                description.length() > 100
                        ? description.substring(0, 100)
                        : description
        );
        order.put("callback_url", callbackUrl);
        order.put("redirect_mode", "TOP_WINDOW");
        order.put("notification_id", ipnNotificationId);
        order.put("billing_address", billing);

        JsonNode response =
                restClient
                        .post()
                        .uri(
                                "/api/Transactions/SubmitOrderRequest"
                        )
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .accept(
                                MediaType.APPLICATION_JSON
                        )
                        .body(order)
                        .retrieve()
                        .body(JsonNode.class);

        if (response == null) {
            throw new RuntimeException(
                    "Empty response from PesaPal"
            );
        }

        String redirectUrl =
                response.path("redirect_url")
                        .asText("");

        if (redirectUrl.isBlank()) {

            String message =
                    response.path("message")
                            .asText(
                                    "PesaPal did not return a payment URL"
                            );

            throw new RuntimeException(message);
        }

        return redirectUrl
                + "\n"
                + response.path("order_tracking_id")
                        .asText("");
    }

    public JsonNode getTransactionStatus(
            String trackingId
    ) {

        validateConfiguration();

        String token = requestToken();

        return restClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path(
                                                "/api/Transactions/GetTransactionStatus"
                                        )
                                        .queryParam(
                                                "orderTrackingId",
                                                trackingId
                                        )
                                        .build()
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .accept(
                        MediaType.APPLICATION_JSON
                )
                .retrieve()
                .body(JsonNode.class);
    }

    private String requestToken() {

        JsonNode response =
                restClient
                        .post()
                        .uri("/api/Auth/RequestToken")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .accept(
                                MediaType.APPLICATION_JSON
                        )
                        .body(
                                Map.of(
                                        "consumer_key",
                                        consumerKey,
                                        "consumer_secret",
                                        consumerSecret
                                )
                        )
                        .retrieve()
                        .body(JsonNode.class);

        if (response == null) {
            throw new RuntimeException(
                    "PesaPal authentication returned no response"
            );
        }

        String token =
                response.path("token")
                        .asText("");

        if (token.isBlank()) {

            throw new RuntimeException(
                    "PesaPal authentication failed: "
                            + response.path("message")
                            .asText("Unknown error")
            );
        }

        return token;
    }

    private void validateConfiguration() {

        if (consumerKey == null
                || consumerKey.isBlank()
                || consumerSecret == null
                || consumerSecret.isBlank()) {

            throw new RuntimeException(
                    "PesaPal credentials are not configured"
            );
        }
    }
}
