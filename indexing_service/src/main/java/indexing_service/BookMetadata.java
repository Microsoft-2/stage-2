package indexing_service;

// A simple record to hold metadata extracted from the _header.txt files.
// We will store a map of <BookID, BookMetadata> in our datamart.
public class BookMetadata {
    // Making fields public for simple Gson serialization
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
    
    // Default constructor for Gson
    public BookMetadata() { }
}
