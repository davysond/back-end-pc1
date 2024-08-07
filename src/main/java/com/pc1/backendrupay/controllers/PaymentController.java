package com.pc1.backendrupay.controllers;

import com.google.gson.JsonSyntaxException;
import com.pc1.backendrupay.domain.RequestPaymentDTO;
import com.pc1.backendrupay.domain.TicketModel;
import com.pc1.backendrupay.domain.UserModel;
import com.pc1.backendrupay.enums.TypeTicket;
import com.pc1.backendrupay.exceptions.UserNotFoundException;
import com.pc1.backendrupay.services.TicketService;
import com.pc1.backendrupay.services.UserService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    @Value("${stripe.endpoint.secret}")
    private String endpointSecret;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    public PaymentController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping("/checkout/{userId}/{typeTicket}")
    private RequestPaymentDTO hostedCheckout(@PathVariable("typeTicket") TypeTicket typeTicket, @PathVariable("userId") UUID userId) throws StripeException, UserNotFoundException {
        Stripe.apiKey = stripeSecretKey;

        String student_lunch_ticket = "price_1P8q7mBo6B2t81e5yglSzDMW";
        String student_dinner_ticket = "price_1P8q8zBo6B2t81e5AooceLcg";
        String external_lunch_ticket = "price_1P8pw2Bo6B2t81e57QS53n1I";
        String external_dinner_ticket = "price_1P8q79Bo6B2t81e5O5rtwzQs";
        String scholarship_lunch_ticket = "price_1P8q9oBo6B2t81e5q31Eat9u";
        String scholarship_dinner_ticket = "price_1P8q9oBo6B2t81e5q31Eat9u";



        String priceId;

        switch(typeTicket){
            case STUDENT_LUNCH_TICKET -> priceId = student_lunch_ticket;
            case STUDENT_DINNER_TICKET -> priceId = student_dinner_ticket;
            case EXTERNAL_LUNCH_TICKET -> priceId = external_lunch_ticket;
            case EXTERNAL_DINNER_TICKET -> priceId = external_dinner_ticket;
            case SCHOLARSHIP_LUNCH_TICKET -> priceId = scholarship_lunch_ticket;
            case SCHOLARSHIP_DINNER_TICKET -> priceId = scholarship_dinner_ticket;
            default -> priceId = external_dinner_ticket;
        }

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setClientReferenceId(userId.toString())  // Adicionando client_reference_id
                        .setSuccessUrl("https://rupayufcg.vercel.app/my-tickets")
                        .setCancelUrl("https://rupayufcg.vercel.app/my-tickets")
                        .build();

        Session session = Session.create(params);
        PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
        String paymentId = paymentIntent.getId();

        UserModel user = userService.getUserId(userId);

        TicketModel newTicket = ticketService.createTicket(userId, typeTicket, paymentId);

        RequestPaymentDTO rpDTO = new RequestPaymentDTO(session.getUrl(), userId);

        return rpDTO;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Stripe.apiKey = stripeSecretKey;
        Event event;

        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (JsonSyntaxException e) {
            // Payload inválido
            System.out.println("⚠️  Webhook error while parsing basic request.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        if (endpointSecret != null && sigHeader != null) {
            // Verificar o evento se um endpoint secret estiver definido
            try {
                event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            } catch (SignatureVerificationException e) {
                // Assinatura inválida
                System.out.println("⚠️  Webhook error while validating signature.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
            }
        }

        // Desserializar o objeto aninhado dentro do evento
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Falha na desserialização
            System.out.println("⚠️  Deserialization failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        // Tratar o evento
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                System.out.println("Payment for " + paymentIntent.getAmount() + " succeeded.");
                // Chamar método para tratar o sucesso do pagamento
                handlePaymentIntentSucceeded(paymentIntent);
                break;
            case "payment_method.attached":
                PaymentMethod paymentMethod = (PaymentMethod) stripeObject;
                // Chamar método para tratar o anexo bem-sucedido de um método de pagamento
                handlePaymentMethodAttached(paymentMethod);
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
                break;
        }

        return ResponseEntity.ok("Event processed");
    }



    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        // Lógica para tratar o sucesso do pagamento
        this.ticketService.setTicketIntentSucceeded(paymentIntent);
    }

    private void handlePaymentMethodAttached(PaymentMethod paymentMethod) {
        // Lógica para tratar o anexo do PaymentMethod
    }
}
