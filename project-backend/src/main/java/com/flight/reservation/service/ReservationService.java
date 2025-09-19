package com.flight.reservation.service;

import com.flight.reservation.dto.ReservationRequest;
import com.flight.reservation.dto.ReservationResponse;
import com.flight.reservation.entity.Reservation;
import com.flight.reservation.entity.Vol;
import com.flight.reservation.enums.StatutReservation;
import com.flight.reservation.event.ReservationEvent;
import com.flight.reservation.exception.PlacesInsuffisantesException;
import com.flight.reservation.exception.ReservationConflictException;
import com.flight.reservation.exception.VolNotFoundException;
import com.flight.reservation.iservice.IReservationService;
import com.flight.reservation.repository.ReservationRepository;
import com.flight.reservation.repository.VolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ReservationService implements IReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final VolRepository volRepository;
    private final VolService volService;
    private final ApplicationEventPublisher eventPublisher;

    public ReservationService(ReservationRepository reservationRepository, VolRepository volRepository, VolService volService, ApplicationEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.volRepository = volRepository;
        this.volService = volService;
        this.eventPublisher = eventPublisher;
    }

    @Retryable(retryFor = {OptimisticLockingFailureException.class, ReservationConflictException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2))
    public ReservationResponse creerReservation(ReservationRequest request) {
        UUID volId = request.getVolId();
        String emailPassager = request.getPassager().getEmail();
        Integer nombrePlaces = request.getNombrePlaces();
        logger.info("Tentative de réservation - Vol: {}, Passager: {}, Places: {}", volId, emailPassager, nombrePlaces);

        Vol vol = null;
        try {
            // Récupération du vol avec verrouillage optimiste
            vol = volRepository.findByIdWithOptimisticLock(volId)
                    .orElseThrow(() -> new VolNotFoundException(volId));
            Integer placesDisponiblesAvant = vol.getPlacesDisponibles();

            // Vérification de la disponibilité
            if (!vol.hasAvailableSeats(nombrePlaces)) {
                publishAuditEvent(volId, emailPassager, nombrePlaces, placesDisponiblesAvant,
                        StatutReservation.FAILED, "Places insuffisantes", null);
                throw new PlacesInsuffisantesException(placesDisponiblesAvant, nombrePlaces);
            }

            // Réservation des places
            vol.reservePlaces(nombrePlaces);
            volRepository.save(vol);

            // Création de la réservation
            Reservation reservation = new Reservation(vol, request.getPassager(), nombrePlaces);
            reservation = reservationRepository.save(reservation);

            // Éviction du cache
            volService.evictCache(volId);

            // Audit de succès
            publishAuditEvent(volId, emailPassager, nombrePlaces, placesDisponiblesAvant, StatutReservation.SUCCESS, null, reservation.getId());

            logger.info("Réservation créée avec succès - ID: {}, Vol: {}", reservation.getId(), volId);
            return new ReservationResponse(
                    reservation.getId(),
                    volId,
                    request.getPassager(),
                    nombrePlaces,
                    reservation.getCreatedAt()
            );
        } catch (OptimisticLockingFailureException e) {
            logger.warn("Conflit de concurrence détecté pour le vol: {}", volId);
            publishAuditEvent(volId, emailPassager, nombrePlaces, null, StatutReservation.FAILED, "Conflit de concurrence", null);
            throw new ReservationConflictException("Conflit détecté, veuillez réessayer", e);
        } catch (Exception e) {
            logger.error("Erreur lors de la réservation pour le vol: {}", volId, e);
            Integer placesDisponiblesAvant = (vol != null) ? vol.getPlacesDisponibles() : 0;
            publishAuditEvent(volId, emailPassager, nombrePlaces, placesDisponiblesAvant, StatutReservation.FAILED, e.getMessage(), null);
            throw e;
        }
    }

    private void publishAuditEvent(UUID volId, String emailPassager, Integer placesDemandees, Integer placesDisponiblesAvant, StatutReservation statut, String messageErreur, UUID reservationId) {
        ReservationEvent event = new ReservationEvent(
                this,
                volId,
                emailPassager,
                placesDemandees,
                placesDisponiblesAvant,
                statut,
                messageErreur,
                reservationId
        );
        eventPublisher.publishEvent(event);
    }
}