package ingestion_service;

import io.javalin.http.Context;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IngestionController {
    public static void ingestBook(Context ctx) throws IOException {
        String bookId = ctx.pathParam("book_id");
        String apiUrl = "https://gutendex.com/books/" + bookId;

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        
        String jsonResponse = new String(conn.getInputStream().readAllBytes());
        BookInfo bookInfo = BookParser.extractBookInfo(jsonResponse, bookId); // Usar el parser
        
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hourPart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
        
        String path = "datalake/" + datePart + "/" + hourPart + "/" + bookId;
        Path bookDir = Paths.get(path);
        Files.createDirectories(bookDir);

        Files.writeString(bookDir.resolve(bookId + "_header.txt"), jsonResponse);

        try {
            String fullText = Downloader.downloadText(bookInfo.fullTextUrl);
            Files.writeString(bookDir.resolve(bookId + "_body.txt"), fullText);
        } catch (IOException e) {
            System.err.println("Error downloading full text for book " + bookId + ": " + e.getMessage());
            ctx.status(500).json(Map.of("book_id", Integer.parseInt(bookId), "status", "download_failed"));
            return;
        }

        ctx.json(Map.of(
            "book_id", Integer.parseInt(bookId),
            "status", "downloaded",
            "path", path
        ));
    }    


public static void checkStatus(Context ctx) throws IOException {
    String bookId = ctx.pathParam("book_id");
    
    boolean found = false;
    try (var stream = Files.walk(Paths.get("datalake"), 4)) {
        found = stream.filter(Files::isRegularFile)
                      .anyMatch(p -> p.getFileName().toString().startsWith(bookId + "_"));
    }
    
    String status = found ? "available" : "not_found";
    
    ctx.json(Map.of(
        "book_id", Integer.parseInt(bookId),
        "status", status
    ));
}

public static void listBooks(Context ctx) throws IOException {
    Path datalake = Paths.get("datalake");
    Set<Integer> bookIds = new HashSet<>();

    if (Files.exists(datalake)) {
        try (var stream = Files.walk(datalake)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> p.getFileName().toString().endsWith("_body.txt")) // Solo contar si el body existe
                  .forEach(p -> {
                      String fileName = p.getFileName().toString();
                      String idString = fileName.split("_")[0];
                      try {
                          bookIds.add(Integer.parseInt(idString));
                      } catch (NumberFormatException ignored) {}
                  });
        }
    }

    ctx.json(Map.of(
        "count", bookIds.size(),
        "books", new ArrayList<>(bookIds)
    ));
}
}