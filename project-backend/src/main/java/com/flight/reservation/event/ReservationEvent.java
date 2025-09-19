package com.flight.reservation.event;

import com.flight.reservation.enums.StatutReservation;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ReservationEvent extends ApplicationEvent {
    
    private final UUID volId;
    private final String emailPassager;
    private final Integer placesDemandees;
    private final Integer placesDisponiblesAvant;
    private final StatutReservation statut;
    private final String messageErreur;
    private final UUID reservationId;
    
    public ReservationEvent(Object source, UUID volId, String emailPassager, 
                           Integer placesDemandees, Integer placesDisponiblesAvant, 
                           StatutReservation statut, String messageErreur, UUID reservationId) {
        super(source);
        this.volId = volId;
        this.emailPassager = emailPassager;
        this.placesDemandees = placesDemandees;
        this.placesDisponiblesAvant = placesDisponiblesAvant;
        this.statut = statut;
        this.messageErreur = messageErreur;
        this.reservationId = reservationId;
    }
    
    // Getters
    public UUID getVolId() { return volId; }
    public String getEmailPassager() { return emailPassager; }
    public Integer getPlacesDemandees() { return placesDemandees; }
    public Integer getPlacesDisponiblesAvant() { return placesDisponiblesAvant; }
    public StatutReservation getStatut() { return statut; }
    public String getMessageErreur() { return messageErreur; }
    public UUID getReservationId() { return reservationId; }
}