package controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {

    private static final Path CONTROL_DIR = Paths.get("control");
    private static final Path DOWNLOADED_FILE = CONTROL_DIR.resolve("downloaded_books.txt");
    private static final Path INDEXED_FILE = CONTROL_DIR.resolve("indexed_books.txt");

    // Configuración para la selección aleatoria:
    private static final int MAX_BOOK_ID = 5000;      // Rango máximo de IDs disponibles en la "biblioteca"
    private static final int BOOKS_TO_INGEST = 10;    // Número de libros aleatorios a seleccionar en cada ejecución

    public static void main(String[] args) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        Files.createDirectories(CONTROL_DIR);


        Random random = new Random();
        Set<Integer> randomBookIds = new HashSet<>();

        while (randomBookIds.size() < BOOKS_TO_INGEST) {
            randomBookIds.add(random.nextInt(MAX_BOOK_ID) + 1);
        }

        List<String> bookIds = randomBookIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        System.out.println("Books selected for ingestion: " + bookIds.size() + " IDs (" + String.join(", ", bookIds) + ")");

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

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("\n Total execution time: " + totalTime + " ms");

        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory();
        long usedMemoryMB = usedMemoryBytes / (1024 * 1024);

        System.out.println("Memory usage (Allocated - Free): " + usedMemoryMB + " MB");
    }
}