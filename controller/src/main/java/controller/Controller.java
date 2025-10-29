package controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

public class Controller {

    private static final Path CONTROL_DIR = Paths.get("control");
    private static final Path DOWNLOADED_FILE = CONTROL_DIR.resolve("downloaded_books.txt");
    private static final Path INDEXED_FILE = CONTROL_DIR.resolve("indexed_books.txt");

    public static void main(String[] args) throws IOException, InterruptedException {
        Files.createDirectories(CONTROL_DIR);

        List<String> bookIds = List.of("1342", "84", "2701");

        HttpClient client = HttpClient.newHttpClient();

        List<String> downloaded = new ArrayList<>();
        for (String bookId : bookIds) {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7001/ingest/" + bookId))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("Ingested " + bookId + ": " + resp.body());
            downloaded.add(bookId);
        }

        Files.write(DOWNLOADED_FILE, downloaded);

        HttpRequest indexReq = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:7002/index/rebuild"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> indexResp = client.send(indexReq, HttpResponse.BodyHandlers.ofString());
        System.out.println("Indexing result: " + indexResp.body());

        Files.write(INDEXED_FILE, downloaded);
        System.out.println("Controller finished processing books.");
    }
}
