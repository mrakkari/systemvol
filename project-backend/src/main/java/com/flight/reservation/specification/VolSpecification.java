package com.flight.reservation.specification;

import com.flight.reservation.entity.Vol;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VolSpecification {

    public static Specification<Vol> hasDateDepart(LocalDateTime dateDepart) {
        if (dateDepart == null) {
            return null;
        }
        LocalDate searchDate = dateDepart.toLocalDate();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("dateDepart").as(LocalDate.class),
                        searchDate
                );
    }

    public static Specification<Vol> hasDateArrivee(LocalDateTime dateArrivee) {
        if (dateArrivee == null) {
            return null;
        }
        LocalDate searchDate = dateArrivee.toLocalDate();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("dateArrivee").as(LocalDate.class),
                        searchDate
                );
    }

    public static Specification<Vol> hasVilleDepart(String villeDepart) {
        return (root, query, criteriaBuilder) ->
                villeDepart == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("villeDepart")),
                        "%" + villeDepart.toLowerCase() + "%"
                );
    }

    public static Specification<Vol> hasVilleArrivee(String villeArrivee) {
        return (root, query, criteriaBuilder) ->
                villeArrivee == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("villeArrivee")),
                        "%" + villeArrivee.toLowerCase() + "%"
                );
    }
}