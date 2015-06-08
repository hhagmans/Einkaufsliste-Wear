package fh_dortmund_hagmans.einkauf.models;

/** Stellt eine Kategorie eines Artikels dar
 * @author Hendrik Hagmans
 */
public enum Category {

    FLEISCHFISCH("Fleisch und Fisch"), GEMUESEOBST("Gemüse und Obst"), KOCHENBACKEN(
            "Kochen und Backen"), MILCHPRODUKTE("Milchprodukte"), TIEFKUEHLPRODUKTE(
            "Tiefkühlprodukte"), GETRAENKE("Getränke"), SUESSIGKEITEN(
            "Süßigkeiten"), KOERPERPFLEGE("Körperpflege"), HAUSHALT("Haushalt"), SONSTIGES(
            "Sonstiges");

    private String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
