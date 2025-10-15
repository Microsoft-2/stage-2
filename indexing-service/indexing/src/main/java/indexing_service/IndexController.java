package Microsoft-2-main.stage2.indexing-service.indexing.src.main.java.indexing_service;

import io.javalin.http.Context;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;

public class IndexController {

    public static void rebuildIndex(Context ctx) throws IOException {
        // IndexController.rebuildIndex
        Map<String, Map<Integer, Integer>> invertedIndex = IndexBuilder.buildIndex("datalake");
        String json = new Gson().toJson(invertedIndex);
        Files.writeString(Paths.get("datamart/inverted_index.json"), json);
        ctx.json(Map.of("status", "ok", "words", invertedIndex.size()));
    }
    public static void status(Context ctx) {
        ctx.json(Map.of("status", "ready"));
    }
}