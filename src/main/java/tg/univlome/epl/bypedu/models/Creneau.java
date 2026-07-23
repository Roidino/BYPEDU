package tg.univlome.epl.bypedu.models;

/**
 * Représente un créneau de l'emploi du temps d'une classe.
 *
 * @author BYPEDU
 */
public class Creneau {

    private int id;
    private int classeId;
    private String classe;
    private int coursId;
    private String cours;       // intitulé du cours
    private String enseignant;  // nom de l'enseignant qui donne le cours
    private String jour;
    private String heureDebut;
    private String heureFin;
    private String salle;

    public Creneau() {
    }

    public Creneau(int id, int classeId, String classe, int coursId, String cours,
                    String enseignant, String jour, String heureDebut, String heureFin, String salle) {
        this.id = id;
        this.classeId = classeId;
        this.classe = classe;
        this.coursId = coursId;
        this.cours = cours;
        this.enseignant = enseignant;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.salle = salle;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClasseId() { return classeId; }
    public void setClasseId(int classeId) { this.classeId = classeId; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public String getCours() { return cours; }
    public void setCours(String cours) { this.cours = cours; }

    public String getEnseignant() { return enseignant; }
    public void setEnseignant(String enseignant) { this.enseignant = enseignant; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public String getHeureDebut() { return heureDebut; }
    public void setHeureDebut(String heureDebut) { this.heureDebut = heureDebut; }

    public String getHeureFin() { return heureFin; }
    public void setHeureFin(String heureFin) { this.heureFin = heureFin; }

    public String getSalle() { return salle; }
    public void setSalle(String salle) { this.salle = salle; }
}
