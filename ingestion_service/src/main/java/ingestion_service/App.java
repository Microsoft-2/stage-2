package ingestion_service;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import com.google.gson.Gson;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinGson(new Gson()));
            config.http.defaultContentType = "application/json";
        }).start(7001);

        app.post("/ingest/{book_id}", IngestionController::ingestBook);
        app.get("/ingest/status/{book_id}", IngestionController::checkStatus);
        app.get("/ingest/list", IngestionController::listBooks);
    }
}