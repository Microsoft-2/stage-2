package search_service;

// This class MUST match the structure of BookMetadata.java in the
// Indexing Service, as it will be used to deserialize metadata.json.
public class BookMetadata {
    public int bookId;
    public String title;
    public String author;
    public String language;
    public int year;

    // Default constructor for Gson
    public BookMetadata() { }

    // (You can add getters/setters if you prefer)
}
