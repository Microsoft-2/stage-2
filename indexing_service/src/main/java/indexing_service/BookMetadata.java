package indexing_service;

public class BookMetadata {
    public int bookId;
    public String title;
    public String author;
    public String language;
    public int year;

    public BookMetadata(int bookId, String title, String author, String language, int year) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.language = language;
        this.year = year;
    }
    
    public BookMetadata() { }
}
