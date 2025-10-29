package indexing_service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class IndexBuilder {

    private static final Gson GSON = new Gson();

    public record IndexResult(
        Map<String, Map<Integer, Integer>> invertedIndex,
        Map<Integer, BookMetadata> metadata
    ) {}

    public static IndexResult buildIndex(String datalakeDir) throws IOException {
        Map<String, Map<Integer, Integer>> index = new HashMap<>();
        Map<Integer, BookMetadata> metadata = new HashMap<>();

        try (var stream = Files.walk(Paths.get(datalakeDir))) {
            stream.filter(Files::isRegularFile)
                  .forEach(p -> {
                      if (p.toString().endsWith("_body.txt")) {
                          processBodyFile(p, index);
                      } else if (p.toString().endsWith("_header.txt")) {
                          processHeaderFile(p, metadata);
                      }
                  });
        }
        return new IndexResult(index, metadata);
    }

    public static void processBodyFile(Path path, Map<String, Map<Integer, Integer>> index) {
        try {
            String content = Files.readString(path);
            int bookId = Integer.parseInt(path.getFileName().toString().split("_")[0]);

            for (String word : content.split("\\s+")) {
                word = word.toLowerCase().replaceAll("[.,!?;\"()\\[\\]{}]", "");
                if (word.isBlank()) continue;

                index.putIfAbsent(word, new HashMap<>());
                Map<Integer, Integer> counts = index.get(word);
                counts.put(bookId, counts.getOrDefault(bookId, 0) + 1);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error processing body file " + path + ": " + e.getMessage());
        }
    }

    public static void processHeaderFile(Path path, Map<Integer, BookMetadata> metadata) {
        try {
            String content = Files.readString(path);
            int bookId = Integer.parseInt(path.getFileName().toString().split("_")[0]);
            
            JsonObject json = GSON.fromJson(content, JsonObject.class);
            
            String title = json.has("title") ? json.get("title").getAsString() : "Unknown Title";
            String author = "Unknown Author";
            if (json.has("authors") && json.getAsJsonArray("authors").size() > 0) {
                author = json.getAsJsonArray("authors").get(0).getAsJsonObject().get("name").getAsString();
            }

            String language = "en"; // Default
            if (json.has("languages") && json.getAsJsonArray("languages").size() > 0) {
                language = json.getAsJsonArray("languages").get(0).getAsString();
            }

            int year = 0; // Default
            if (json.has("publication_year")) { // Using this field as it's common
                year = json.get("publication_year").getAsInt();
            } else if (json.has("copyright") && !json.get("copyright").isJsonNull())

            metadata.put(bookId, new BookMetadata(bookId, title, author, language, year));
            
        } catch (Exception e) {
            System.err.println("Error processing header file " + path + ": " + e.getMessage());
        }
    }
}
