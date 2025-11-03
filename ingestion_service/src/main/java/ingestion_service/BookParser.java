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
                String key = entry.getKey();
                if (key.startsWith("text/plain")) {
                    fullTextUrl = entry.getValue().getAsString();

                    // TODO: Priorizar una versión específica (como us-ascii o utf-8) si existen varias.
                    // Por simplicidad, tomamos el primero que sea 'text/plain'.
                    break;
                }
            }
        }

        if (fullTextUrl == null) {
            System.err.println("Advertencia: No se encontró el URL del archivo .txt para el Book ID: " + bookId);
        }

        return new BookInfo(bookId, title, author, fullTextUrl);
    }
}