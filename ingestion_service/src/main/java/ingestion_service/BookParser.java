package ingestion_service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;

public class BookParser {
    private static final Gson GSON = new Gson();

    public static BookInfo extractBookInfo(String jsonResponse, String bookId) {
        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);

        String title = "Unknown Title";
        if (jsonObject.has("title")) {
            title = jsonObject.get("title").getAsString();
        }

        String author = "Unknown Author";
        if (jsonObject.has("authors") && jsonObject.getAsJsonArray("authors").size() > 0) {
            author = jsonObject.getAsJsonArray("authors")
                    .get(0).getAsJsonObject()
                    .get("name").getAsString();
        }

        String fullTextUrl = null;

        if (jsonObject.has("formats")) {
            JsonObject formats = jsonObject.getAsJsonObject("formats");

            String url_utf8 = null;
            String url_ascii = null;
            String url_other_txt = null;

            for (Map.Entry<String, com.google.gson.JsonElement> entry : formats.entrySet()) {
                String key = entry.getKey();
                String url = entry.getValue().getAsString();

                if (key.startsWith("text/plain") && (url.endsWith(".txt") || url.endsWith(".txt.utf-8"))) {

                    // Prioridad 1: UTF-8
                    if (key.contains("utf-8") || key.equals("text/plain")) {
                        url_utf8 = url;
                    }
                    // Prioridad 2: US-ASCII
                    else if (key.contains("us-ascii")) {
                        url_ascii = url;
                    }
                    // Prioridad 3: Cualquier otro text/plain que sea .txt
                    else {
                        url_other_txt = url;
                    }
                }
            }

            if (url_utf8 != null) {
                fullTextUrl = url_utf8;
            } else if (url_ascii != null) {
                fullTextUrl = url_ascii;
            } else if (url_other_txt != null) {
                fullTextUrl = url_other_txt;
            }
        }

        if (fullTextUrl == null) {
            System.err.println("Advertencia: No se encontró ningún URL de .txt válido para el Book ID: " + bookId);
        }

        return new BookInfo(bookId, title, author, fullTextUrl);
    }
}