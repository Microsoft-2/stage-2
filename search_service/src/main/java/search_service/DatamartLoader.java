package search_service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.HashMap;

public class DatamartLoader {

    // Path to the datamart created by the Indexing Service
    private static final Path DATAMART_DIR = Paths.get("datamart");
    private static final Path INVERTED_INDEX_FILE = DATAMART_DIR.resolve("inverted_index.json");
    private static final Path METADATA_FILE = DATAMART_DIR.resolve("metadata.json");
    
    private static final Gson GSON = new Gson();

    // A record to hold both datamart structures
    public record Datamart(
        Map<String, Map<Integer, Integer>> invertedIndex,
        Map<Integer, BookMetadata> metadata
    ) {}

    public static Datamart load() throws IOException {
        System.out.println("Loading datamarts from " + DATAMART_DIR.toAbsolutePath());

        Map<String, Map<Integer, Integer>> invertedIndex;
        Map<Integer, BookMetadata> metadata;

        if (Files.exists(INVERTED_INDEX_FILE)) {
            String indexJson = Files.readString(INVERTED_INDEX_FILE);
            invertedIndex = GSON.fromJson(indexJson, new TypeToken<HashMap<String, Map<Integer, Integer>>>() {}.getType());
        } else {
            System.err.println("Warning: " + INVERTED_INDEX_FILE + " not found. Starting with empty index.");
            invertedIndex = new HashMap<>();
        }

        if (Files.exists(METADATA_FILE)) {
            String metaJson = Files.readString(METADATA_FILE);
            metadata = GSON.fromJson(metaJson, new TypeToken<HashMap<Integer, BookMetadata>>() {}.getType());
        } else {
            System.err.println("Warning: " + METADATA_FILE + " not found. Starting with empty metadata.");
            metadata = new HashMap<>();
        }

        System.out.println("Loaded " + metadata.size() + " books and " + invertedIndex.size() + " words.");
        return new Datamart(invertedIndex, metadata);
    }
}
