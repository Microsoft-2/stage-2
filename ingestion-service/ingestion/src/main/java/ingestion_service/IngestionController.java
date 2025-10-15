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

        // 1. Obtener Metadatos JSON (Header)
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");
        
        String jsonResponse = new String(conn.getInputStream().readAllBytes());
        BookInfo bookInfo = BookParser.extractBookInfo(jsonResponse, bookId); // Usar el parser
        
        // 2. Definir y crear la ruta jerárquica (ej. datalake/20251008/14/1342)
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hourPart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"));
        
        String path = "datalake/" + datePart + "/" + hourPart + "/" + bookId;
        Path bookDir = Paths.get(path);
        Files.createDirectories(bookDir);

        // 3. Descargar y Guardar Header
        Files.writeString(bookDir.resolve(bookId + "_header.txt"), jsonResponse);

        // 4. Descargar y Guardar Body (Nueva Lógica)
        try {
            String fullText = Downloader.downloadText(bookInfo.fullTextUrl);
            Files.writeString(bookDir.resolve(bookId + "_body.txt"), fullText);
        } catch (IOException e) {
            // Manejar errores de descarga (puedes loguearlo o devolver un estado de error)
            System.err.println("Error downloading full text for book " + bookId + ": " + e.getMessage());
            ctx.status(500).json(Map.of("book_id", Integer.parseInt(bookId), "status", "download_failed"));
            return;
        }

        // 5. Respuesta Estándar Exitosa [cite: 186-190]
        ctx.json(Map.of(
            "book_id", Integer.parseInt(bookId),
            "status", "downloaded",
            "path", path // Ruta final del datalake
        ));
    }    
}

public static void checkStatus(Context ctx) throws IOException {
    String bookId = ctx.pathParam("book_id");
    
    // Buscar el archivo del libro en todo el datalake
    boolean found = false;
    try (var stream = Files.walk(Paths.get("datalake"), 4)) { // Buscar hasta 4 niveles de profundidad (año/mes/dia/id)
        found = stream.filter(Files::isRegularFile)
                      .anyMatch(p -> p.getFileName().toString().startsWith(bookId + "_"));
    }
    
    String status = found ? "available" : "not_found";
    
    // Respuesta Estándar [cite: 192-194]
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
                      // Usar expresión regular o lógica robusta para extraer el ID
                      String fileName = p.getFileName().toString();
                      String idString = fileName.split("_")[0];
                      try {
                          bookIds.add(Integer.parseInt(idString));
                      } catch (NumberFormatException ignored) {}
                  });
        }
    }

    // Respuesta Estándar [cite: 197-199]
    ctx.json(Map.of(
        "count", bookIds.size(),
        "books", new ArrayList<>(bookIds)
    ));
}
}
