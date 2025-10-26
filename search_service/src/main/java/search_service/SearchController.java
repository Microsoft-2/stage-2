package search_service;

import io.javalin.http.Context;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchController {

    private static DatamartLoader.Datamart datamart;

    // Load the datamart into memory when the controller is initialized
    static {
        try {
            datamart = DatamartLoader.load();
        } catch (IOException e) {
            System.err.println("Failed to load datamart! Search will not work.");
            e.printStackTrace();
            datamart = new DatamartLoader.Datamart(new HashMap<>(), new HashMap<>());
        }
    }

    public static void search(Context ctx) {
        // 1. Get all query parameters [cite: 231, 238, 252, 263]
        String query = ctx.queryParam("q");
        String author = ctx.queryParam("author");
        String language = ctx.queryParam("language");
        String yearStr = ctx.queryParam("year");

        // 2. Build the filters map for the response [cite: 241, 255, 267]
        Map<String, Object> filters = new HashMap<>();
        if (author != null) filters.put("author", author);
        if (language != null) filters.put("language", language);
        if (yearStr != null) filters.put("year", Integer.parseInt(yearStr));

        // 3. Perform the search
        Set<Integer> matchingBookIds;
        
        if (query == null || query.isBlank()) {
            // If no query, return all books (and filter them)
            matchingBookIds = new HashSet<>(datamart.metadata().keySet());
        } else {
            // Find book IDs matching the query term(s)
            matchingBookIds = findBookIdsByQuery(datamart, query.toLowerCase());
        }

        // 4. Filter the results
        List<BookMetadata> filteredResults = matchingBookIds.stream()
            .map(id -> datamart.metadata().get(id))
            .filter(Objects::nonNull) // Ensure metadata exists
            .filter(book -> (author == null || book.author.equalsIgnoreCase(author)))
            .filter(book -> (language == null || book.language.equalsIgnoreCase(language)))
            .filter(book -> (yearStr == null || book.year == Integer.parseInt(yearStr)))
            .collect(Collectors.toList());

        // 5. Build the final JSON response [cite: 233-237]
        ctx.json(Map.of(
            "query", query != null ? query : "all",
            "filters", filters,
            "count", filteredResults.size(),
            "results", filteredResults
        ));
    }

    /**
     * Finds the set of book IDs that contain ALL terms in the query.
     */
    public static Set<Integer> findBookIdsByQuery(DatamartLoader.Datamart localDatamart, String query) {
        String[] terms = query.split("\\s+");
        if (terms.length == 0) {
            return new HashSet<>();
        }

        // Get the set of books for the first term
        Set<Integer> results = new HashSet<>(
            localDatamart.invertedIndex().getOrDefault(terms[0], new HashMap<>()).keySet()
        );
        
        // Intersect this set with the sets for all other terms
        for (int i = 1; i < terms.length; i++) {
            Set<Integer> termBooks = localDatamart.invertedIndex().getOrDefault(terms[i], new HashMap<>()).keySet();
            results.retainAll(termBooks); // Keep only elements present in both sets
        }

        return results;
    }
}
