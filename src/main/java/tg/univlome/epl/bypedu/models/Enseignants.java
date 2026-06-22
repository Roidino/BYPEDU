package tg.univlome.epl.bypedu.models;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Honoré
 */

public class Enseignants {
    // 1. Les attributs (qui correspondent aux colonnes de ta base de données)
    private int id;
    private String nom;
    private String prenom;
    private String matiere;
    private String classe;
    private String email;
    private String telephone;

    // 2. Le constructeur vide (obligatoire pour beaucoup de frameworks et DAOs)
    public Enseignants() {
    }

    // 3. Le constructeur complet (pratique pour créer un enseignant rapidement)
    public Enseignants(int id, String nom,  String prenom, String matiere, String classe, String email, String telephone) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.matiere = matiere;
        this.classe = classe;
        this.email = email;
        this.telephone = telephone;
    }

    // 4. Les Getters et Setters (Indispensables pour que le TableView de JavaFX puisse lire les données)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}