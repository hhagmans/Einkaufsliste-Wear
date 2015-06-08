package fh_dortmund_hagmans.einkauf.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Stellt eine ShoppingList mit Artikeln dar
 * @author Hendrik Hagmans
 */
public class ShoppingList {

    private int id;
    private Date date;


    private List<Article> articles = new ArrayList<Article>();;

    public ShoppingList() {

    }

    public ShoppingList(Date date) {
        this.date = date;
    }

    public ShoppingList(Date date, List<Article> articles) {
        this.date = date;
        this.articles = articles;
    }


    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public boolean containsArticle(String name, Category category) {
        boolean contains = false;
        for (Article article : getArticles()) {
            if (article.getName().equals(name)
                    && article.getCategory().equals(category)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    public Article getArticle(String name, Category category) {
        Article returnarticle = null;
        for (Article article : getArticles()) {
            if (article.getName().equals(name)
                    && article.getCategory().equals(category)) {
                returnarticle = article;
                break;
            }
        }
        return returnarticle;
    }

    public void addArticle(Article article) {
        this.articles.add(article);
    }

    public void addArticles(List<Article> articles) {
        for (Article article : articles) {
            addArticle(article);
        }
    }
}
