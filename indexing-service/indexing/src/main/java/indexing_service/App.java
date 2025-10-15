// App.java
package indexing_service;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson; // Importar para usar Gson

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // Configurar Javalin para usar Gson
            config.jsonMapper(new JavalinGson(new com.google.gson.Gson())); 
            config.http.defaultContentType = "application/json"; // Establecer tipo de contenido por defecto [cite: 125]
        }).start(7002); 
        
        // Registrar todos los endpoints
        app.post("/index/update/{book_id}", IndexController::updateIndex); // ¡ENDPOINT FALTANTE!
        app.post("/index/rebuild", IndexController::rebuildIndex);
        app.get("/index/status", IndexController::status);
    }
}