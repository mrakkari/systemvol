package com.flight.reservation.controller;

import com.flight.reservation.dto.VolRequest;
import com.flight.reservation.entity.Vol;
import com.flight.reservation.iservice.IVolService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vols")
public class VolController {

    private final IVolService volService;

    public VolController(IVolService volService) {
        this.volService = volService;
    }

    @GetMapping
    public ResponseEntity<List<Vol>> getVols(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureDepart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateArrivee,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureArrivee,
            @RequestParam(required = false) String villeDepart,
            @RequestParam(required = false) String villeArrivee,
            @RequestParam(required = false) String tri) {

        // Convert LocalDate + LocalTime to LocalDateTime for backward compatibility
        LocalDateTime dateTimeDepart = (dateDepart != null) ? LocalDateTime.of(dateDepart, heureDepart != null ? heureDepart : LocalTime.of(0, 0)) : null;
        LocalDateTime dateTimeArrivee = (dateArrivee != null) ? LocalDateTime.of(dateArrivee, heureArrivee != null ? heureArrivee : LocalTime.of(0, 0)) : null;

        List<Vol> vols = volService.findAll(dateTimeDepart, dateTimeArrivee, villeDepart, villeArrivee, tri);
        return ResponseEntity.ok(vols);
    }

    @PostMapping
    public ResponseEntity<List<Vol>> addVols(@Valid @RequestBody List<VolRequest> volRequests) {
        List<Vol> savedVols = volService.saveAll(volRequests);
        return new ResponseEntity<>(savedVols, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/places")
    public ResponseEntity<Integer> getPlacesDisponibles(@PathVariable UUID id) {
        Integer places = volService.getPlacesDisponibles(id);
        return ResponseEntity.ok(places);
    }
}