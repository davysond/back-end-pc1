package com.pc1.backendrupay.repositories;

import com.pc1.backendrupay.domain.TicketModel;
import com.pc1.backendrupay.enums.TypeTicket;
import com.pc1.backendrupay.enums.statusTicket.StatusTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<TicketModel, UUID> {

    @Query("SELECT t FROM TicketModel t WHERE t.id = :id")
    List<TicketModel> findByUserId(UUID id);

    List<TicketModel> findByTypeTicket(TypeTicket typeTicket);
    List<TicketModel> findByStatusTicket(StatusTicket statusTicket);
    List<TicketModel> findByTypeTicketAndStatusTicket(TypeTicket typeTicket, StatusTicket statusTicket);
    List<TicketModel> findByPurchaseDate(LocalDateTime purchaseDate);
    List<TicketModel> findByPrice(Double price);
    List<TicketModel> findByTypeTicketAndStatusTicketAndPurchaseDateAndPrice(
            TypeTicket typeTicket,
            StatusTicket statusTicket,
            LocalDateTime purchaseDate,
            Double price
    );
}
