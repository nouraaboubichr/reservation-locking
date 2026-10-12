package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationRetryHandler;
import com.example.service.ReservationService;
import com.example.util.HibernateUtil;
import org.h2.tools.Server;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

public class App {

    private static Reservation creer(ReservationService rs) {
        LocalDateTime debut = LocalDateTime.now().plusDays(1);
        Reservation r = new Reservation("Salle A", debut, debut.plusHours(2));
        rs.create(r);
        return r;
    }

    public static void main(String[] args) {

        // ----- Console H2 (au début) -----
        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("Console H2 disponible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage de la console H2");
            e.printStackTrace();
        }

        ReservationService rs = new ReservationService();
        ReservationRetryHandler retry = new ReservationRetryHandler(rs);

        // ============ Scénario 1 : conflit détecté, sans retry ============
        System.out.println("\n===== 1. Conflit de version (sans retry) =====");
        Reservation r1 = creer(rs);
        System.out.println("Création : " + r1);

        Reservation tx1 = rs.findById(r1.getId());
        Reservation tx2 = rs.findById(r1.getId());
        System.out.println("Transaction 1 charge : " + tx1);
        System.out.println("Transaction 2 charge : " + tx2);

        tx1.setSalle("Salle B");
        rs.modifier(tx1);
        System.out.println("Transaction 1 : modification réussie -> " + tx1);

        try {
            tx2.setSalle("Salle C");
            rs.modifier(tx2);
            System.out.println("Transaction 2 : modification réussie.");
        } catch (OptimisticLockException e) {
            System.out.println("Transaction 2 : conflit détecté -> " + e.getMessage());
        }
        System.out.println("État en base : " + rs.findById(r1.getId()));

        // ============ Scénario 2 : conflit résolu par retry ============
        System.out.println("\n===== 2. Conflit résolu avec retry =====");
        Reservation r2 = creer(rs);
        System.out.println("Création : " + r2);

        Reservation t1 = rs.findById(r2.getId());
        Reservation t2 = rs.findById(r2.getId());

        t1.setSalle("Salle B");
        rs.modifier(t1);
        System.out.println("Transaction 1 : modification réussie -> " + t1);

        Reservation resultat = retry.modifierAvecRetry(t2, r -> r.setSalle("Salle C"));
        System.out.println("Transaction 2 : modification réussie après retry -> " + resultat);
        System.out.println("État en base : " + rs.findById(r2.getId()));

        // ----- Garder la console H2 ouverte (à la fin) -----
        System.out.println("\nAppuyez sur Entrée pour arrêter le programme et la console H2...");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        HibernateUtil.shutdown();
        System.exit(0);
    }
}