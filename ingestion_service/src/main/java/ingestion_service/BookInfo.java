package ingestion_service;
// Clase para contener los datos extraídos
public class BookInfo {
    public final String bookId;
    public final String title;
    public final String author;
    public final String fullTextUrl;

    public BookInfo(String bookId, String title, String author, String fullTextUrl) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.fullTextUrl = fullTextUrl;
    }
}