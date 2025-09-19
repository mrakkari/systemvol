package com.flight.reservation.dto;

import com.flight.reservation.entity.Passager;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReservationResponse {
    
    private UUID numeroReservation;
    private UUID volId;
    private Passager passager;
    private Integer nombrePlaces;
    private LocalDateTime dateReservation;
    
    // Constructeurs
    public ReservationResponse() {}
    
    public ReservationResponse(UUID numeroReservation, UUID volId, Passager passager, 
                              Integer nombrePlaces, LocalDateTime dateReservation) {
        this.numeroReservation = numeroReservation;
        this.volId = volId;
        this.passager = passager;
        this.nombrePlaces = nombrePlaces;
        this.dateReservation = dateReservation;
    }
    
    // Getters et Setters
    public UUID getNumeroReservation() { return numeroReservation; }
    public void setNumeroReservation(UUID numeroReservation) { this.numeroReservation = numeroReservation; }
    
    public UUID getVolId() { return volId; }
    public void setVolId(UUID volId) { this.volId = volId; }
    
    public Passager getPassager() { return passager; }
    public void setPassager(Passager passager) { this.passager = passager; }
    
    public Integer getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(Integer nombrePlaces) { this.nombrePlaces = nombrePlaces; }
    
    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }
}