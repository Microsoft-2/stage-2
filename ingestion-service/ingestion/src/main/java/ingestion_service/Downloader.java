package ingestion_service;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
    /**
     * Descarga el contenido de texto plano desde un URL.
     * @param urlString El URL del archivo .txt del libro.
     * @return El contenido completo del archivo como una cadena.
     * @throws IOException si la conexión falla o el código de respuesta no es 200.
     */
    public static String downloadText(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to download text from " + urlString + 
                                  ". HTTP response code: " + conn.getResponseCode());
        }

        // Lee y devuelve el contenido del InputStream
        try (var inputStream = conn.getInputStream()) {
            return new String(inputStream.readAllBytes());
        } finally {
            conn.disconnect();
        }
    }
}