package com.flight.reservation.entity;

import com.flight.reservation.enums.StatutReservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "vol_id", nullable = false)
    private UUID volId;
    
    @Column(name = "email_passager", nullable = false, length = 100)
    private String emailPassager;
    
    @Column(name = "places_demandees", nullable = false)
    private Integer placesDemandees;
    
    @Column(name = "places_disponibles_avant", nullable = false)
    private Integer placesDisponiblesAvant;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutReservation statut;
    
    @Column(name = "message_erreur", length = 500)
    private String messageErreur;
    
    @Column(name = "reservation_id")
    private UUID reservationId;
    
    // Constructeurs
    public AuditLog() {}
    
    public AuditLog(UUID volId, String emailPassager, Integer placesDemandees, 
                   Integer placesDisponiblesAvant, StatutReservation statut, 
                   String messageErreur, UUID reservationId) {
        this.volId = volId;
        this.emailPassager = emailPassager;
        this.placesDemandees = placesDemandees;
        this.placesDisponiblesAvant = placesDisponiblesAvant;
        this.statut = statut;
        this.messageErreur = messageErreur;
        this.reservationId = reservationId;
    }
    
    // Getters et Setters



}