package ingestion_service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;

public class BookParser {
    private static final Gson GSON = new Gson();
    public static BookInfo extractBookInfo(String jsonResponse, String bookId) {
        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        
        String title = jsonObject.get("title").getAsString();
        String author = "Unknown"; // Valor por defecto
        
        if (jsonObject.has("authors") && jsonObject.getAsJsonArray("authors").size() > 0) {
            author = jsonObject.getAsJsonArray("authors")
                               .get(0).getAsJsonObject()
                               .get("name").getAsString();
        }

        String fullTextUrl = null;
        if (jsonObject.has("formats")) {
            JsonObject formats = jsonObject.getAsJsonObject("formats");
            
            for (Map.Entry<String, com.google.gson.JsonElement> entry : formats.entrySet()) {
                if (entry.getKey().endsWith(".txt")) {
                    fullTextUrl = entry.getValue().getAsString();
                    break;
                }
            }
        }

        // No lanzamos RuntimeException, simplemente devolvemos null
        // o un valor vacío para que el IngestionController pueda manejarlo.
        if (fullTextUrl == null) {
            System.err.println("Advertencia: No se encontró el URL del archivo .txt para el Book ID: " + bookId);
        }

        // Devolvemos la BookInfo; si fullTextUrl es null, el Downloader fallará,
        // pero el IngestionController lo manejará con un estado 500 más limpio.
        return new BookInfo(bookId, title, author, fullTextUrl);
    }
}