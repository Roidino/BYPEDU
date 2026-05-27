/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.dao;

/**
 *
 * @author user
 */

import tg.univlome.epl.bypedu.model.Cours;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    // Données fictives (à remplacer par DB plus tard)
    private static List<Cours> coursList = new ArrayList<>();
    private static int nextId = 7;

    static {
        coursList.add(new Cours(1, "Mathématiques Avancées", "Terminale S", "Dr. Michel Fontaine", 6, 7));
        coursList.add(new Cours(2, "Littérature Française",  "Première L",  "Sophie Girard",       5, 5));
        coursList.add(new Cours(3, "Physique Quantique",     "Terminale S", "Jean Roux",            4, 6));
        coursList.add(new Cours(4, "Histoire Contemporaine", "Terminale ES","Isabelle Mercier",     4, 4));
        coursList.add(new Cours(5, "Anglais Avancé",         "Toutes classes","Marc Vincent",       3, 3));
        coursList.add(new Cours(6, "Chimie Organique",       "Terminale S", "Jean Roux",            3, 5));
    }

    public List<Cours> findAll() {
        return new ArrayList<>(coursList);
    }

    public void save(Cours cours) {
        cours.setId(nextId++);
        coursList.add(cours);
    }

    public void update(Cours cours) {
        for (int i = 0; i < coursList.size(); i++) {
            if (coursList.get(i).getId() == cours.getId()) {
                coursList.set(i, cours);
                return;
            }
        }
    }

    public void delete(int id) {
        coursList.removeIf(c -> c.getId() == id);
    }
}
