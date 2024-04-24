package com.pc1.backendrupay.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pc1.backendrupay.enums.TypeTicket;
import com.pc1.backendrupay.enums.statusTicket.StatusTicket;
import jakarta.persistence.*;
import lombok.*;
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

    public TicketModel(Double price, TypeTicket typeTicket, StatusTicket statusTicket){
        this.price = price;
        this.typeTicket = typeTicket;
        this.statusTicket = statusTicket;
    }


}
