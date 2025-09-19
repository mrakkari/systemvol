package com.flight.reservation.iservice;


import com.flight.reservation.dto.ReservationRequest;
import com.flight.reservation.dto.ReservationResponse;

public interface IReservationService {
    ReservationResponse creerReservation(ReservationRequest request);
}