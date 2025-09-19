package com.flight.reservation.service;

import com.flight.reservation.dto.ReservationRequest;
import com.flight.reservation.dto.ReservationResponse;
import com.flight.reservation.entity.Passager;
import com.flight.reservation.entity.Reservation;
import com.flight.reservation.entity.Vol;
import com.flight.reservation.exception.PlacesInsuffisantesException;
import com.flight.reservation.exception.VolNotFoundException;
import com.flight.reservation.repository.ReservationRepository;
import com.flight.reservation.repository.VolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private VolRepository volRepository;

    @Mock
    private VolService volService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private Vol vol;
    private ReservationRequest reservationRequest;

    @BeforeEach
    void setUp() {
        vol = new Vol(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                "Paris",
                "Lyon",
                new BigDecimal("150.00"),
                120,
                180
        );
        vol.setId(UUID.randomUUID());

        Passager passager = new Passager("Dupont", "Jean", "jean.dupont@email.com");
        reservationRequest = new ReservationRequest(vol.getId(), passager, 2);

        // Reset mocks to avoid interference between tests
        reset(eventPublisher);
    }

    @Test
    void should_create_reservation_when_seats_available() {
        // Given
        when(volRepository.findByIdWithOptimisticLock(vol.getId())).thenReturn(Optional.of(vol));

        Reservation savedReservation = new Reservation(vol, reservationRequest.getPassager(), 2);
        savedReservation.setId(UUID.randomUUID());

        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // When
        ReservationResponse response = reservationService.creerReservation(reservationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNumeroReservation()).isEqualTo(savedReservation.getId());
        assertThat(response.getNombrePlaces()).isEqualTo(2);
        assertThat(vol.getPlacesReservees()).isEqualTo(2);

        verify(volRepository).save(vol);
        verify(reservationRepository).save(any(Reservation.class));
        verify(volService).evictCache(vol.getId());
        // Only one event published for successful reservation
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void should_throw_exception_when_vol_not_found() {
        // Given
        when(volRepository.findByIdWithOptimisticLock(vol.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.creerReservation(reservationRequest))
                .isInstanceOf(VolNotFoundException.class);

        // Only one event published in catch block for vol not found
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void should_throw_exception_when_insufficient_seats() {
        // Given
        vol.setPlacesReservees(179); // Only 1 seat available
        when(volRepository.findByIdWithOptimisticLock(vol.getId())).thenReturn(Optional.of(vol));

        // When & Then
        assertThatThrownBy(() -> reservationService.creerReservation(reservationRequest))
                .isInstanceOf(PlacesInsuffisantesException.class)
                .hasMessageContaining("Places insuffisantes");

        // Two events: one for insufficient seats, one in catch block
        verify(eventPublisher, times(2)).publishEvent(any());
        verify(reservationRepository, never()).save(any());
    }
}