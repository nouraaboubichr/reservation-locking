package com.example.service;

import com.example.model.Reservation;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;

import javax.persistence.OptimisticLockException;

public class ReservationService extends AbstractFacade<Reservation> {

    public ReservationService() {
        super(Reservation.class);
    }

    /**
     * Modifie une réservation avec verrouillage optimiste.
     * L'UPDATE est exécuté avec la version de l'objet reçu : "... where id=? and version=?".
     * Si une autre transaction est passée entre-temps, aucune ligne n'est modifiée
     * et une OptimisticLockException est levée.
     */
    public Reservation modifier(Reservation reservation) {
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.update(reservation);

            tx.commit();
            return reservation;
        } catch (RuntimeException e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {
                    // transaction déjà terminée
                }
            }
            if (estConflit(e)) {
                throw new OptimisticLockException("Conflit de version : la réservation "
                        + reservation.getId() + " a été modifiée par une autre transaction", e);
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /** Selon la version d'Hibernate, l'exception est enveloppée ou non : on parcourt la chaîne des causes. */
    private static boolean estConflit(Throwable e) {
        while (e != null) {
            if (e instanceof OptimisticLockException || e instanceof StaleStateException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }
}