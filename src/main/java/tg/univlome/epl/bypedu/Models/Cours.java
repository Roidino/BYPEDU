/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.Models;

/**
 *
 * @author user
 */
public class Cours {

    private int id;
    private String intitule;
    private String classe;
    private String enseignant;
    private int volumeHoraire;
    private int coefficient;

    public Cours() {}

    public Cours(int id, String nom, String classe, String enseignant,
                 int volumeHoraire, int coefficient) {
        this.id = id;
        this.intitule = nom;
        this.classe = classe;
        this.enseignant = enseignant;
        this.volumeHoraire = volumeHoraire;
        this.coefficient = coefficient;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String nom) { this.intitule = nom; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public String getEnseignant() { return enseignant; }
    public void setEnseignant(String enseignant) { this.enseignant = enseignant; }

    public int getVolumeHoraire() { return volumeHoraire; }
    public void setVolumeHoraire(int volumeHoraire) { this.volumeHoraire = volumeHoraire; }

    public int getCoefficient() { return coefficient; }
    public void setCoefficient(int coefficient) { this.coefficient = coefficient; }
}