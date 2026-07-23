package tg.univlome.epl.bypedu.models;

/**
 * Représente une note attribuée à un étudiant dans une matière pour un trimestre donné.
 *
 * @author BYPEDU
 */
public class Note {

    private int id;
    private int etudiantId;
    private String etudiant;   // nom complet affiché dans le tableau
    private String classe;     // classe de l'étudiant (affichage)
    private int matiereId;
    private String matiere;    // nom de la matière affiché dans le tableau
    private double note;
    private String trimestre;
    private String tendance;   // HAUSSE, BAISSE, STABLE

    public Note() {
    }

    public Note(int id, int etudiantId, String etudiant, String classe, int matiereId,
                String matiere, double note, String trimestre, String tendance) {
        this.id = id;
        this.etudiantId = etudiantId;
        this.etudiant = etudiant;
        this.classe = classe;
        this.matiereId = matiereId;
        this.matiere = matiere;
        this.note = note;
        this.trimestre = trimestre;
        this.tendance = tendance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEtudiantId() { return etudiantId; }
    public void setEtudiantId(int etudiantId) { this.etudiantId = etudiantId; }

    public String getEtudiant() { return etudiant; }
    public void setEtudiant(String etudiant) { this.etudiant = etudiant; }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    public int getMatiereId() { return matiereId; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public String getTrimestre() { return trimestre; }
    public void setTrimestre(String trimestre) { this.trimestre = trimestre; }

    public String getTendance() { return tendance; }
    public void setTendance(String tendance) { this.tendance = tendance; }
}
