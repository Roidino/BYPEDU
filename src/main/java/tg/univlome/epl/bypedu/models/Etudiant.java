/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tg.univlome.epl.bypedu.models;

import java.time.LocalDate;

/**
 *
 * @author Savastano
 */
public class Etudiant {
    private int id;
    private String nom;
    private String prenom;
    private int telephone;
    private LocalDate date_naissance;
    private int classe_id;
    private String status;
    private LocalDate date_inscription;

    public Etudiant() {
    }

    public Etudiant(int id, String nom, String prenom, int telephone, LocalDate date_naissance, int classe_id, String status, LocalDate date_inscription) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.date_naissance = date_naissance;
        this.classe_id = classe_id;
        this.status = status;
        this.date_inscription = date_inscription;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public int getTelephone() {
        return telephone;
    }

    public LocalDate getDate_naissance() {
        return date_naissance;
    }

    public int getClasse_id() {
        return classe_id;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDate_inscription() {
        return date_inscription;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public void setDate_naissance(LocalDate date_naissance) {
        this.date_naissance = date_naissance;
    }

    public void setClasse_id(int classe_id) {
        this.classe_id = classe_id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDate_inscription(LocalDate date_inscription) {
        this.date_inscription = date_inscription;
    }

    @Override
    public String toString() {
        return "Etudiant{" + "id=" + id + ", nom=" + nom + ", prenom=" + prenom + ", telephone=" + telephone + ", date_naissance=" + date_naissance + ", classe_id=" + classe_id + ", status=" + status + ", date_inscription=" + date_inscription + '}';
    }

    
}
