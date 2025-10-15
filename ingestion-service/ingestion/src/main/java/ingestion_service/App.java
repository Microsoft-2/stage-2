package ingestion_service;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7001);
        app.post("/ingest/:book_id", IngestionController::ingestBook);
        app.get("/ingest/status/:book_id", IngestionController::checkStatus);
        app.get("/ingest/list", IngestionController::listBooks);
    }
}
