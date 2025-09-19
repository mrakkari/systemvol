package com.flight.reservation.exception;

import java.util.UUID;

public class VolNotFoundException extends RuntimeException {
    
    public VolNotFoundException(UUID volId) {
        super("Vol avec l'ID " + volId + " non trouvé");
    }
    
    public VolNotFoundException(String message) {
        super(message);
    }
}