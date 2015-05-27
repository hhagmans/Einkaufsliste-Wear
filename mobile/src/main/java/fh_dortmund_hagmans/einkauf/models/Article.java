package fh_dortmund_hagmans.einkauf.models;

/**
 * Created by hendrikh on 24.04.15.
 */
public class Article {

    private int id;
    private String name;
    private Category category;
    private boolean checked = false;

    public Article() {

    }

    public Article(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isChecked() {
        return checked;
    }

    public void checkArticle() {
        this.checked = true;
    }

    public void uncheckArticle() {
        this.checked = false;
    }
}
