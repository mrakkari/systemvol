package com.flight.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class VolRequest {

    @NotNull
    private LocalDate dateDepart;

    @NotNull
    private LocalDate dateArrivee;

    @NotNull
    private String villeDepart;

    @NotNull
    private String villeArrivee;

    @NotNull
    @Positive
    private BigDecimal prix;

    @NotNull
    @Positive
    private Integer tempsTrajet;

    @Positive
    private Integer capaciteMaximale = 180;

    // Constructeurs
    public VolRequest() {}

    public VolRequest(LocalDate dateDepart, LocalDate dateArrivee,
                      String villeDepart, String villeArrivee, BigDecimal prix,
                      Integer tempsTrajet, Integer capaciteMaximale) {
        this.dateDepart = dateDepart;
        this.dateArrivee = dateArrivee;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.prix = prix;
        this.tempsTrajet = tempsTrajet;
        this.capaciteMaximale = capaciteMaximale;
    }

    // Alternative constructor for backward compatibility
    public VolRequest(LocalDateTime dateDepart, LocalDateTime dateArrivee,
                      String villeDepart, String villeArrivee, BigDecimal prix,
                      Integer tempsTrajet, Integer capaciteMaximale) {
        this.dateDepart = dateDepart.toLocalDate();
        this.dateArrivee = dateArrivee.toLocalDate();
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.prix = prix;
        this.tempsTrajet = tempsTrajet;
        this.capaciteMaximale = capaciteMaximale;
    }

    // Getters et Setters
    public LocalDate getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }

    public LocalDate getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDate dateArrivee) { this.dateArrivee = dateArrivee; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public Integer getTempsTrajet() { return tempsTrajet; }
    public void setTempsTrajet(Integer tempsTrajet) { this.tempsTrajet = tempsTrajet; }

    public Integer getCapaciteMaximale() { return capaciteMaximale; }
    public void setCapaciteMaximale(Integer capaciteMaximale) { this.capaciteMaximale = capaciteMaximale; }

    // Utility methods (default time = 00:00)
    public LocalDateTime getDateTimeDepart() {
        return LocalDateTime.of(dateDepart, LocalTime.of(0, 0));
    }

    public LocalDateTime getDateTimeArrivee() {
        return LocalDateTime.of(dateArrivee, LocalTime.of(0, 0));
    }
}
