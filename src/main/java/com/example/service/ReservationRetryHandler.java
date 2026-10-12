package com.example.service;

import com.example.model.Reservation;

import javax.persistence.OptimisticLockException;
import java.util.function.Consumer;

public class ReservationRetryHandler {

    private static final int MAX_TENTATIVES = 3;

    private final ReservationService service;

    public ReservationRetryHandler(ReservationService service) {
        this.service = service;
    }

    /**
     * Applique "modification" sur la réservation et l'enregistre.
     * En cas de conflit : recharge la dernière version, réapplique la modification, réessaie.
     */
    public Reservation modifierAvecRetry(Reservation reservation, Consumer<Reservation> modification) {
        Reservation courante = reservation;

        for (int tentative = 1; tentative <= MAX_TENTATIVES; tentative++) {
            try {
                modification.accept(courante);
                return service.modifier(courante);
            } catch (OptimisticLockException e) {
                System.out.println("  Conflit détecté (tentative " + tentative + "/" + MAX_TENTATIVES
                        + "), rechargement de la dernière version...");
                courante = service.findById(reservation.getId());
                if (courante == null) {
                    throw new IllegalStateException("La réservation " + reservation.getId() + " a été supprimée");
                }
            }
        }
        throw new OptimisticLockException("Échec après " + MAX_TENTATIVES + " tentatives");
    }
}