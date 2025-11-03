package search_service;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinGson());
            config.http.defaultContentType = "application/json";
        }).start(7003);
        
        app.get("/search", SearchController::search);
    }
}
