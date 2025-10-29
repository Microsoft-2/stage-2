package search_service;

import io.javalin.http.Context;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SearchController {

    private static DatamartLoader.Datamart datamart;

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
        String query = ctx.queryParam("q");
        String author = ctx.queryParam("author");
        String language = ctx.queryParam("language");
        String yearStr = ctx.queryParam("year");

        Map<String, Object> filters = new HashMap<>();
        if (author != null) filters.put("author", author);
        if (language != null) filters.put("language", language);
        if (yearStr != null) filters.put("year", Integer.parseInt(yearStr));

        Set<Integer> matchingBookIds;
        
        if (query == null || query.isBlank()) {
            matchingBookIds = new HashSet<>(datamart.metadata().keySet());
        } else {
            matchingBookIds = findBookIdsByQuery(datamart, query.toLowerCase());
        }

        List<BookMetadata> filteredResults = matchingBookIds.stream()
            .map(id -> datamart.metadata().get(id))
            .filter(Objects::nonNull)
            .filter(book -> (author == null || book.author.equalsIgnoreCase(author)))
            .filter(book -> (language == null || book.language.equalsIgnoreCase(language)))
            .filter(book -> (yearStr == null || book.year == Integer.parseInt(yearStr)))
            .collect(Collectors.toList());

        ctx.json(Map.of(
            "query", query != null ? query : "all",
            "filters", filters,
            "count", filteredResults.size(),
            "results", filteredResults
        ));
    }


    public static Set<Integer> findBookIdsByQuery(DatamartLoader.Datamart localDatamart, String query) {
        String[] terms = query.split("\\s+");
        if (terms.length == 0) {
            return new HashSet<>();
        }

        Set<Integer> results = new HashSet<>(
            localDatamart.invertedIndex().getOrDefault(terms[0], new HashMap<>()).keySet()
        );
        
        for (int i = 1; i < terms.length; i++) {
            Set<Integer> termBooks = localDatamart.invertedIndex().getOrDefault(terms[i], new HashMap<>()).keySet();
            results.retainAll(termBooks); // Keep only elements present in both sets
        }

        return results;
    }
}
