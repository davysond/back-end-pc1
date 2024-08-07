package com.pc1.backendrupay.services;

import com.pc1.backendrupay.domain.TicketModel;
import com.pc1.backendrupay.domain.UserModel;
import com.pc1.backendrupay.enums.TypeTicket;
import com.pc1.backendrupay.enums.TypeUser;
import com.pc1.backendrupay.enums.statusTicket.StatusTicket;
import com.pc1.backendrupay.exceptions.UserNotFoundException;
import com.pc1.backendrupay.repositories.TicketRepository;
import com.pc1.backendrupay.repositories.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService{

    public static double LUNCH_PRICE = 11.45;
    public static double DINNER_PRICE = 11.90;

    @Autowired
    final private TicketRepository ticketRepository;

    @Autowired
    final private UserRepository userRepository;

    @Autowired
    final private UserService userService;

    public TicketServiceImpl(TicketRepository ticketRepository, UserService userService, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public List<TicketModel> listTickets(TypeTicket typeTicket, StatusTicket statusTicket,
                                         LocalDateTime purchaseDate, Double price) {
        if (typeTicket != null && statusTicket != null && purchaseDate != null && price != null) {
            return ticketRepository.findByTypeTicketAndStatusTicketAndPurchaseDateAndPrice(
                    typeTicket, statusTicket, purchaseDate, price
            );
        } else if (typeTicket != null && statusTicket != null) {
            return ticketRepository.findByTypeTicketAndStatusTicket(typeTicket, statusTicket);
        } else if (purchaseDate != null) {
            return ticketRepository.findByPurchaseDate(purchaseDate);
        } else if (price != null) {
            return ticketRepository.findByPrice(price);
        } else if (typeTicket != null) {
            return ticketRepository.findByTypeTicket(typeTicket);
        } else if (statusTicket != null) {
            return ticketRepository.findByStatusTicket(statusTicket);
        } else {
            return ticketRepository.findAll();
        }
    }

    public TicketModel buyTicket(UUID id, TypeTicket typeTicket, String paymentID) throws UserNotFoundException, StripeException {
        UserModel user = userService.getUserId(id);
        if (user.getTickets() == null) {
            user.setTickets(new ArrayList<TicketModel>());
        }

        if (user.getTypeUser() == TypeUser.STUDENT) {
            LUNCH_PRICE = 5.72;
            DINNER_PRICE = 5.45;
        } else if (user.getTypeUser() == TypeUser.SCHOLARSHIP_STUDENT) {
            LUNCH_PRICE = 0.0;
            DINNER_PRICE = 0.0;
        } else {
            LUNCH_PRICE = LUNCH_PRICE;
            DINNER_PRICE = DINNER_PRICE;
        }

        Double price = typeTicket == TypeTicket.LUNCH ? LUNCH_PRICE : DINNER_PRICE;

        TicketModel ticket = new TicketModel(price, typeTicket, StatusTicket.INACTIVE, paymentID);
        ticketRepository.save(ticket);
        user.getTickets().add(ticket);
        userService.saveUser(user);
        return ticket;

    }
    public TicketModel createTicket(UUID id, TypeTicket typeTicket, String paymentID) throws UserNotFoundException, StripeException {
        TicketModel newTicket = buyTicket(id, typeTicket, paymentID);
        return newTicket;
    }

    public List<TicketModel> listTicketByUserId(UUID id, StatusTicket statusTicket) throws UserNotFoundException {
        UserModel user = userService.getUserId(id);
        List<TicketModel> tickets = user.getTickets();
        if (statusTicket != null) {
            List<TicketModel> filteredTickets = new ArrayList<>();
            for (TicketModel ticket : tickets) {
                if (ticket.getStatusTicket() == statusTicket) {
                    filteredTickets.add(ticket);
                }
            }
            return filteredTickets;
        }
        return tickets;
    }

    public Optional<TicketModel> consultTicketById(UUID id){
        Optional<TicketModel> ticket = ticketRepository.findById(id);
        return ticket;
    }

    public List<TicketModel> listTicketsActives(UUID id) throws UserNotFoundException {
        UserModel user = userService.getUserId(id);
        List<TicketModel> allTickets = user.getTickets();
        List<TicketModel> activesTickets = new ArrayList<>();

        for (TicketModel t : allTickets){
            if (t.getStatusTicket() == StatusTicket.ACTIVE){
                activesTickets.add(t);
            }
        }

        return activesTickets;
    }

    @Override
    public void updateTicketStatusToActive(UUID ticketId) {
        Optional<TicketModel> ticket = ticketRepository.findById(ticketId);
        if (ticket.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }
        TicketModel newTicket = ticket.get();
        newTicket.setStatusTicket(StatusTicket.ACTIVE);
        ticketRepository.save(newTicket);
    }

    @Override
    public void updateTicketStatusToInactive(UUID ticketId) {
        Optional<TicketModel> ticket = ticketRepository.findById(ticketId);
        if (ticket.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }
        TicketModel newTicket = ticket.get();
        newTicket.setStatusTicket(StatusTicket.INACTIVE);
        ticketRepository.save(newTicket);

    }

    @Override
    public void setTicketIntentSucceeded(PaymentIntent paymentIntent) {
        String paymentId = paymentIntent.getId();
        Optional<TicketModel> ticket = ticketRepository.findByPaymentID(paymentId);
        if (ticket.isEmpty()) {
            throw new RuntimeException("Ticket not found");
        }
        TicketModel newTicket = ticket.get();
        newTicket.setStatusTicket(StatusTicket.ACTIVE);
        ticketRepository.save(newTicket);
    }


}
