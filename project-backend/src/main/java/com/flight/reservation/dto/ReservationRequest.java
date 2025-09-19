package com.flight.reservation.dto;

import com.flight.reservation.entity.Passager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
public class ReservationRequest {
    
    @NotNull
    private UUID volId;
    
    @Valid
    @NotNull
    private Passager passager;
    
    @NotNull
    @Positive
    private Integer nombrePlaces;
    
    // Constructeurs
    public ReservationRequest() {}
    
    public ReservationRequest(UUID volId, Passager passager, Integer nombrePlaces) {
        this.volId = volId;
        this.passager = passager;
        this.nombrePlaces = nombrePlaces;
    }
    
    // Getters et Setters

}