package com.flight.reservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vol_id", nullable = false)
    @JsonIgnore
    @NotNull
    private Vol vol;
    
    @Embedded
    @Valid
    @NotNull
    private Passager passager;
    
    @NotNull
    @Positive
    @Column(name = "nombre_places", nullable = false)
    private Integer nombrePlaces;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructeurs
    public Reservation() {}
    
    public Reservation(Vol vol, Passager passager, Integer nombrePlaces) {
        this.vol = vol;
        this.passager = passager;
        this.nombrePlaces = nombrePlaces;
    }
    
    // Getters et Setters

}