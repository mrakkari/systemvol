package com.flight.reservation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "vols")
public class Vol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotNull
    @Column(name = "date_depart", nullable = false)
    private LocalDateTime dateDepart;
    
    @NotNull
    @Column(name = "date_arrivee", nullable = false)
    private LocalDateTime dateArrivee;
    
    @NotNull
    @Column(name = "ville_depart", nullable = false, length = 100)
    private String villeDepart;
    
    @NotNull
    @Column(name = "ville_arrivee", nullable = false, length = 100)
    private String villeArrivee;
    
    @NotNull
    @Positive
    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;
    
    @NotNull
    @Positive
    @Column(name = "temps_trajet", nullable = false)
    private Integer tempsTrajet; // in minutes
    
    @NotNull
    @Positive
    @Column(name = "capacite_maximale", nullable = false)
    private Integer capaciteMaximale = 180;
    
    @NotNull
    @Column(name = "places_reservees", nullable = false)
    private Integer placesReservees = 0;
    
    @Version
    @Column(name = "version")
    private Long version = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "vol", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();
    
    // Constructeurs
    public Vol() {}
    
    public Vol(LocalDateTime dateDepart, LocalDateTime dateArrivee, String villeDepart, 
               String villeArrivee, BigDecimal prix, Integer tempsTrajet, Integer capaciteMaximale) {
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.prix = prix;
        this.tempsTrajet = tempsTrajet;
        this.capaciteMaximale = capaciteMaximale != null ? capaciteMaximale : 180;
    }
    
    // Méthodes métier
    public Integer getPlacesDisponibles() {
        return capaciteMaximale - placesReservees;
    }
    
    public boolean hasAvailableSeats(Integer nombrePlaces) {
        return getPlacesDisponibles() >= nombrePlaces;
    }
    
    public void reservePlaces(Integer nombrePlaces) {
        if (!hasAvailableSeats(nombrePlaces)) {
            throw new IllegalStateException("Pas assez de places disponibles");
        }
        this.placesReservees += nombrePlaces;
    }
    
    // Getters et Setters
    public UUID getId() { return id; }



}