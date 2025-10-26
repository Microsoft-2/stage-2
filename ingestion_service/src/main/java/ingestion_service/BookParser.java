package ingestion_service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;

public class BookParser {
    private static final Gson GSON = new Gson();
    /**
     * Extrae el URL del texto plano (.txt) del JSON de Gutendex.
     * @param jsonResponse La cadena JSON de la respuesta de Gutendex.
     * @param bookId El ID del libro que se está procesando.
     * @return Un objeto BookInfo con el URL del texto y otros metadatos clave.
     * @throws RuntimeException si no se encuentra el URL del texto plano.
     */
    public static BookInfo extractBookInfo(String jsonResponse, String bookId) {
        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        
        // 1. Extraer Metadatos Simples
        String title = jsonObject.get("title").getAsString();
        String author = "Unknown"; // Valor por defecto
        
        if (jsonObject.has("authors") && jsonObject.getAsJsonArray("authors").size() > 0) {
            author = jsonObject.getAsJsonArray("authors")
                               .get(0).getAsJsonObject()
                               .get("name").getAsString();
        }

        // 2. Encontrar el URL del texto completo (.txt)
        String fullTextUrl = null;
        if (jsonObject.has("formats")) {
            JsonObject formats = jsonObject.getAsJsonObject("formats");
            
            // Busca la clave que termine en .txt (texto plano)
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