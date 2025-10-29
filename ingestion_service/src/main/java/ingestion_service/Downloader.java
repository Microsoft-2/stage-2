package ingestion_service;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {
    public static String downloadText(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to download text from " + urlString + 
                                  ". HTTP response code: " + conn.getResponseCode());
        }

        try (var inputStream = conn.getInputStream()) {
            return new String(inputStream.readAllBytes());
        } finally {
            conn.disconnect();
        }
    }
}