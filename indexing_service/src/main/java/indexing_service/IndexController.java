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

    public static void rebuildIndex(Context ctx) throws IOException {
        long startTime = System.nanoTime();
        Files.createDirectories(DATAMART_DIR);

        IndexBuilder.IndexResult result = IndexBuilder.buildIndex("datalake");

        Files.writeString(INVERTED_INDEX_FILE, GSON.toJson(result.invertedIndex()));
        
        Files.writeString(METADATA_FILE, GSON.toJson(result.metadata()));
        
        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;

        ctx.json(Map.of(
            "books_processed", result.metadata().size(),
            "elapsed_time", String.format("%.1fs", elapsedSeconds)
        ));
    }

    public static void updateIndex(Context ctx) throws IOException {
        String bookIdStr = ctx.pathParam("book_id");
        int bookId = Integer.parseInt(bookIdStr);

        Map<String, Map<Integer, Integer>> invertedIndex = loadInvertedIndex();
        Map<Integer, BookMetadata> metadata = loadMetadata();

        metadata.remove(bookId);
        for (Map<Integer, Integer> postings : invertedIndex.values()) {
            postings.remove(bookId);
        }
        invertedIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());

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

        if (headerPath != null) {
            IndexBuilder.processHeaderFile(headerPath, metadata);
        }
        if (bodyPath != null) {
            IndexBuilder.processBodyFile(bodyPath, invertedIndex);
        }

        Files.writeString(INVERTED_INDEX_FILE, GSON.toJson(invertedIndex));
        Files.writeString(METADATA_FILE, GSON.toJson(metadata));

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
            Map<Integer, BookMetadata> metadata = loadMetadata();
            booksIndexed = metadata.size();
            
            Instant lastMod = Files.getLastModifiedTime(METADATA_FILE).toInstant();
            lastUpdate = DateTimeFormatter.ISO_INSTANT.format(lastMod);

            long totalBytes = Files.size(INVERTED_INDEX_FILE) + Files.size(METADATA_FILE);
            indexSizeMB = totalBytes / (1024.0 * 1024.0);
        }
        
        ctx.json(Map.of(
            "books_indexed", booksIndexed,
            "last_update", lastUpdate,
            "index_size_MB", indexSizeMB
        ));
    }
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
