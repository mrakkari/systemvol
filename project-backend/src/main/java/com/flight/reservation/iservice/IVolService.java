package com.flight.reservation.iservice;


import com.flight.reservation.dto.VolRequest;
import com.flight.reservation.entity.Vol;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface IVolService {
    List<Vol> findAll(LocalDateTime dateDepart, LocalDateTime dateArrivee, String villeDepart, String villeArrivee, String tri);
    Integer getPlacesDisponibles(UUID volId);
    List<Vol> saveAll(List<VolRequest> volRequests);
    void evictCache(UUID volId);
}
