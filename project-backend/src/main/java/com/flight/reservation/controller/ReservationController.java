package com.flight.reservation.controller;

import com.flight.reservation.dto.ReservationRequest;
import com.flight.reservation.dto.ReservationResponse;
import com.flight.reservation.iservice.IReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final IReservationService reservationService;

    public ReservationController(IReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> creerReservation(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.creerReservation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}