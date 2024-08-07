package com.pc1.backendrupay.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pc1.backendrupay.enums.TypeTicket;
import com.pc1.backendrupay.enums.statusTicket.StatusTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_tickets")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TicketModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private Double price;
    private TypeTicket typeTicket;
    private StatusTicket statusTicket;
    private LocalDateTime purchaseDate;
    private String paymentID;


    public TicketModel(Double price, TypeTicket typeTicket, StatusTicket statusTicket, String paymentID){
        this.price = price;
        this.typeTicket = typeTicket;
        this.statusTicket = statusTicket;
        this.purchaseDate = LocalDateTime.now();
        this.paymentID = paymentID;
    }


}
