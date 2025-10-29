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
        
        if (fullTextUrl == null) {
            throw new RuntimeException("No se encontró el URL del archivo .txt para el Book ID: " + bookId);
        }
        
        return new BookInfo(bookId, title, author, fullTextUrl);
    }
}