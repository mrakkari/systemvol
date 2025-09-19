package com.flight.reservation.exception;

public class PlacesInsuffisantesException extends RuntimeException {
    
    private final Integer placesDisponibles;
    private final Integer placesDemandees;
    
    public PlacesInsuffisantesException(Integer placesDisponibles, Integer placesDemandees) {
        super(String.format("Places insuffisantes. Disponibles: %d, Demandées: %d", 
                          placesDisponibles, placesDemandees));
        this.placesDisponibles = placesDisponibles;
        this.placesDemandees = placesDemandees;
    }
    
    public Integer getPlacesDisponibles() { return placesDisponibles; }
    public Integer getPlacesDemandees() { return placesDemandees; }
}