package com.flight.reservation.service;

import com.flight.reservation.dto.VolRequest;
import com.flight.reservation.entity.Vol;
import com.flight.reservation.iservice.IVolService;
import com.flight.reservation.repository.VolRepository;
import com.flight.reservation.specification.VolSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VolService implements IVolService {
    private final VolRepository volRepository;

    public VolService(VolRepository volRepository) {
        this.volRepository = volRepository;
    }

    @Transactional(readOnly = true)
    public List<Vol> findAll(LocalDateTime dateDepart, LocalDateTime dateArrivee, String villeDepart, String villeArrivee, String tri) {
        Specification<Vol> spec = Specification.where(VolSpecification.hasDateDepart(dateDepart))
                .and(VolSpecification.hasDateArrivee(dateArrivee))
                .and(VolSpecification.hasVilleDepart(villeDepart))
                .and(VolSpecification.hasVilleArrivee(villeArrivee));
        Sort sort = createSort(tri);
        if (sort != null) {
            return volRepository.findAll(spec, sort);
        } else {
            return volRepository.findAll(spec);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vol-places", key = "#volId")
    public Integer getPlacesDisponibles(UUID volId) {
        return volRepository.findById(volId)
                .map(Vol::getPlacesDisponibles)
                .orElse(0);
    }

    public List<Vol> saveAll(List<VolRequest> volRequests) {
        List<Vol> vols = volRequests.stream()
                .map(this::convertToEntity)
                .toList();
        return volRepository.saveAll(vols);
    }
    // to update the cache
    @CacheEvict(value = "vol-places", key = "#volId")
    public void evictCache(UUID volId) {
        // Cache éviction automatique
    }

    private Vol convertToEntity(VolRequest request) {
        return new Vol(
                request.getDateTimeDepart(),
                request.getDateTimeArrivee(),
                request.getVilleDepart(),
                request.getVilleArrivee(),
                request.getPrix(),
                request.getTempsTrajet(),
                request.getCapaciteMaximale()
        );
    }

    private Sort createSort(String tri) {
        if (tri == null) {
            return null;
        }
        return switch (tri.toLowerCase()) {
            case "prix" -> Sort.by("prix").ascending();
            case "tempstrajet" -> Sort.by("tempsTrajet").ascending();
            default -> null;
        };
    }
}