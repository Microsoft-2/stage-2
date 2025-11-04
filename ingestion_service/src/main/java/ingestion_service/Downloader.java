package ingestion_service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader {

    private static final int MAX_REDIRECTS = 5;

    public static String downloadText(String urlString) throws IOException {
        return downloadTextRecursive(urlString, MAX_REDIRECTS);
    }

    private static String downloadTextRecursive(String urlString, int redirectsRemaining) throws IOException {
        if (redirectsRemaining < 0) {
            throw new IOException("Demasiadas redirecciones para: " + urlString);
        }

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        conn.setInstanceFollowRedirects(false);

        int responseCode = conn.getResponseCode();

        switch (responseCode) {
            case HttpURLConnection.HTTP_OK: // 200
                try (InputStream inputStream = conn.getInputStream()) {
                    return new String(inputStream.readAllBytes());
                } finally {
                    conn.disconnect();
                }

            case HttpURLConnection.HTTP_MOVED_PERM: // 301
            case HttpURLConnection.HTTP_MOVED_TEMP: // 302
            case HttpURLConnection.HTTP_SEE_OTHER:  // 303
                String newUrl = conn.getHeaderField("Location");
                conn.disconnect();

                System.out.println("Redirigiendo a: " + newUrl);
                return downloadTextRecursive(newUrl, redirectsRemaining - 1);

            default:
                conn.disconnect();
                throw new IOException("Fallo al descargar texto desde " + urlString +
                        ". Código de respuesta HTTP: " + responseCode);
        }
    }
}