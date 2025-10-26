package indexing_service;

import io.javalin.http.Context;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class IndexController {

    private static final Path DATAMART_DIR = Paths.get("datamart");
    private static final Path INVERTED_INDEX_FILE = DATAMART_DIR.resolve("inverted_index.json");
    private static final Path METADATA_FILE = DATAMART_DIR.resolve("metadata.json");
    private static final Gson GSON = new Gson();

    /**
     * Rebuilds the entire index from scratch. [cite: 212]
     */
    public static void rebuildIndex(Context ctx) throws IOException {
        long startTime = System.nanoTime();
        Files.createDirectories(DATAMART_DIR);

        // 1. Build both indexes from the datalake
        IndexBuilder.IndexResult result = IndexBuilder.buildIndex("datalake");

        // 2. Save the inverted index
        Files.writeString(INVERTED_INDEX_FILE, GSON.toJson(result.invertedIndex()));
        
        // 3. Save the metadata
        Files.writeString(METADATA_FILE, GSON.toJson(result.metadata()));
        
        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;

        // 4. Respond with the correct format [cite: 216-217]
        ctx.json(Map.of(
            "books_processed", result.metadata().size(),
            "elapsed_time", String.format("%.1fs", elapsedSeconds)
        ));
    }

    /**
     * Updates the index for a single book. [cite: 205]
     * NOTE: This is a complex operation. For this project, a common strategy
     * is to load the existing datamarts, remove the old book data,
     * re-process the single book, and save.
     */
    public static void updateIndex(Context ctx) throws IOException {
        String bookIdStr = ctx.pathParam("book_id");
        int bookId = Integer.parseInt(bookIdStr);
        
        // This is a "naive" but correct implementation for the project scope.
        // A more "Big Data" approach would use a database or append-only logs.
        
        // 1. Load existing datamarts
        Map<String, Map<Integer, Integer>> invertedIndex = loadInvertedIndex();
        Map<Integer, BookMetadata> metadata = loadMetadata();

        // 2. Remove old data for this bookId [cite: 227]
        metadata.remove(bookId);
        // Remove from inverted index (this is the slow part)
        for (Map<Integer, Integer> postings : invertedIndex.values()) {
            postings.remove(bookId);
        }
        // Optional: clean up empty words
        invertedIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // 3. Find the book's files (this is slow, but required)
        Path headerPath = null;
        Path bodyPath = null;
        try (var stream = Files.walk(Paths.get("datalake"))) {
            List<Path> paths = stream.filter(p -> p.getFileName().toString().startsWith(bookIdStr + "_"))
                                     .toList();
            for(Path p : paths) {
                if (p.toString().endsWith("_header.txt")) headerPath = p;
                if (p.toString().endsWith("_body.txt")) bodyPath = p;
            }
        }

        // 4. Re-process and add new data
        if (headerPath != null) {
            IndexBuilder.processHeaderFile(headerPath, metadata);
        }
        if (bodyPath != null) {
            IndexBuilder.processBodyFile(bodyPath, invertedIndex);
        }

        // 5. Save datamarts back to disk
        Files.writeString(INVERTED_INDEX_FILE, GSON.toJson(invertedIndex));
        Files.writeString(METADATA_FILE, GSON.toJson(metadata));

        // 6. Respond with success [cite: 209-210]
        ctx.json(Map.of(
            "book_id", bookId,
            "index", "updated"
        ));
    }

    /**
     * Returns statistics about the index. [cite: 220]
     */
    public static void status(Context ctx) throws IOException {
        long booksIndexed = 0;
        String lastUpdate = "N/A";
        double indexSizeMB = 0;

        if (Files.exists(METADATA_FILE) && Files.exists(INVERTED_INDEX_FILE)) {
            // Get book count from metadata file
            Map<Integer, BookMetadata> metadata = loadMetadata();
            booksIndexed = metadata.size();
            
            // Get last update time
            Instant lastMod = Files.getLastModifiedTime(METADATA_FILE).toInstant();
            lastUpdate = DateTimeFormatter.ISO_INSTANT.format(lastMod);

            // Get total size
            long totalBytes = Files.size(INVERTED_INDEX_FILE) + Files.size(METADATA_FILE);
            indexSizeMB = totalBytes / (1024.0 * 1024.0);
        }
        
        // Respond with the correct format [cite: 221-223]
        ctx.json(Map.of(
            "books_indexed", booksIndexed,
            "last_update", lastUpdate,
            "index_size_MB", indexSizeMB
        ));
    }

    // --- Helper Methods ---
    
    private static Map<String, Map<Integer, Integer>> loadInvertedIndex() throws IOException {
        if (!Files.exists(INVERTED_INDEX_FILE)) return new HashMap<>();
        String json = Files.readString(INVERTED_INDEX_FILE);
        return GSON.fromJson(json, new TypeToken<HashMap<String, Map<Integer, Integer>>>() {}.getType());
    }

    private static Map<Integer, BookMetadata> loadMetadata() throws IOException {
        if (!Files.exists(METADATA_FILE)) return new HashMap<>();
        String json = Files.readString(METADATA_FILE);
        return GSON.fromJson(json, new TypeToken<HashMap<Integer, BookMetadata>>() {}.getType());
    }
}
