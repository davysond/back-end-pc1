package com.pc1.backendrupay.services;

import com.pc1.backendrupay.domain.TicketModel;
import com.pc1.backendrupay.enums.TypeTicket;
import com.pc1.backendrupay.enums.statusTicket.StatusTicket;
import com.pc1.backendrupay.exceptions.UserNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    public TicketModel buyTicket(UUID id, TypeTicket typeTicket, String paymentId) throws UserNotFoundException, StripeException;
    public List<TicketModel> listTickets(TypeTicket typeTicket, StatusTicket statusTicket,
                                         LocalDateTime purchaseDate, Double price);
    public List<TicketModel> listTicketByUserId(UUID id, StatusTicket statusTicket) throws UserNotFoundException;
    public Optional<TicketModel> consultTicketById(UUID id);
    public TicketModel createTicket(UUID id, TypeTicket typeTicket, String paymentID) throws UserNotFoundException, StripeException;
    public List<TicketModel> listTicketsActives(UUID id) throws UserNotFoundException;

    void updateTicketStatusToActive(UUID ticketId);

    void updateTicketStatusToInactive(UUID ticketId);

    void setTicketIntentSucceeded(PaymentIntent paymentIntent);

    TicketModel getTicketById(String ticketId);

    void setPaymentId(String ticketId, String paymentIntent);
}
