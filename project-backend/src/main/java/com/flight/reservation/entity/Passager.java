package com.flight.reservation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Passager {
    
    @NotBlank
    @Column(name = "nom", nullable = false, length = 50)
    private String nom;
    
    @NotBlank
    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;
    
    @Email
    @NotBlank
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    // Constructeurs
    public Passager() {}
    
    public Passager(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
    
    // Getters et Setters

    
    public String getNomComplet() {
        return prenom + " " + nom;
    }
}