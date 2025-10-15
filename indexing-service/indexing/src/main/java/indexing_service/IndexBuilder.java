package Microsoft-2-main.stage2.indexing-service.indexing.src.main.java.indexing_service;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class IndexBuilder {

    public static Map<String, Map<Integer, Integer>> buildIndex(String datalakeDir) throws IOException {
        Map<String, Map<Integer, Integer>> index = new HashMap<>();

        try (var stream = Files.walk(Paths.get(datalakeDir))) {
            stream.filter(p -> p.toString().endsWith("_body.txt"))
                  .forEach(p -> processFile(p, index));
        }
        return index;
    }

    private static void processFile(Path path, Map<String, Map<Integer, Integer>> index) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}