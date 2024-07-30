package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.EphemeralKey;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.EphemeralKeyCreateParams;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    public Map<String, String> createPaymentIntent() throws StripeException {
        Map<String, String> response = new HashMap<>();
        try {
            // Create a new customer
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setDescription("Customer for payment")
                    .build();
            Customer customer = Customer.create(customerParams);

            // Create an ephemeral key for the customer
            EphemeralKeyCreateParams ephemeralKeyParams = EphemeralKeyCreateParams.builder()
                    .setCustomer(customer.getId())
                    .build();
            RequestOptions requestOptions = RequestOptions.builder().setStripeVersionOverride("2020-08-27").build();
            EphemeralKey ephemeralKey = EphemeralKey.create(ephemeralKeyParams, requestOptions);

            // Create a payment intent for the customer
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(1099L)
                    .setCurrency("usd")
                    .setCustomer(customer.getId())
                    .addPaymentMethodType("card")
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Prepare the response
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("customer", customer.getId());
            response.put("ephemeralKey", ephemeralKey.getSecret());
        } catch (StripeException e) {
            e.printStackTrace();
            throw e;
        }
        return response;
    }
}