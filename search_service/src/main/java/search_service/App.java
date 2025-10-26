package search_service;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinGson(new com.google.gson.Gson())); 
            config.http.defaultContentType = "application/json";
        }).start(7003); // Running on port 7003 (Ingestion 7001, Indexing 7002)
        
        // Register the search endpoint [cite: 231, 238, 252, 263]
        app.get("/search", SearchController::search);
    }
}
