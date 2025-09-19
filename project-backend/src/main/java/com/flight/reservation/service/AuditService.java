package com.flight.reservation.service;

import com.flight.reservation.entity.AuditLog;
import com.flight.reservation.event.ReservationEvent;
import com.flight.reservation.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReservationEvent(ReservationEvent event) {
        try {
            AuditLog auditLog = new AuditLog(
                event.getVolId(),
                event.getEmailPassager(),
                event.getPlacesDemandees(),
                event.getPlacesDisponiblesAvant(),
                event.getStatut(),
                event.getMessageErreur(),
                event.getReservationId()
            );
            
            auditLogRepository.save(auditLog);
            
            logger.info("Audit log créé pour la tentative de réservation - Vol: {}, Passager: {}, Statut: {}", 
                       event.getVolId(), event.getEmailPassager(), event.getStatut());
                       
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'audit log", e);
        }
    }
}