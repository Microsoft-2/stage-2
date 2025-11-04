# Stage 2 - Distributed Search Engine

**Big Data Course - ULPGC**  
**Group:** Microsoft-2  
**Academic Year:** 2024-2025

## Team Members
- Enrique Padrón
- Jorge Curbero
- Daniel Medina
- Sergio Muela

## Project Overview

This is Stage 2 of our search engine project. We've taken the monolithic data layer from Stage 1 and transformed it into a **distributed microservices architecture**. The system downloads books from Project Gutenberg, indexes them, and provides a search API with filtering capabilities.

### What's New in Stage 2?
- **Microservices**: Split into 3 independent services
- **REST APIs**: All communication via HTTP/JSON
- **Search Engine**: Fast inverted index with filters
- **Benchmarking**: Performance analysis and bottleneck identification

## Architecture

```
                    ┌─────────────┐
                    │   Control   │
                    │   Module    │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Ingestion   │   │   Indexing   │   │    Search    │
│   Service    │   │   Service    │   │   Service    │
│  (Port 7001) │   │  (Port 7002) │   │  (Port 7003) │
└──────┬───────┘   └───────┬──────┘   └──────┬───────┘
       │                   │                  │
       ▼                   ▼                  ▼
   ┌─────────┐       ┌──────────┐       ┌──────────┐
   │Datalake │◄──────┤Datamarts │◄──────┤Datamarts │
   └─────────┘       └──────────┘       └──────────┘
```

### Services

1. **Ingestion Service** - Downloads books from Project Gutenberg and stores them in the datalake
2. **Indexing Service** - Processes books, extracts metadata, and builds inverted indexes
3. **Search Service** - Provides query API with keyword search and filters
4. **Control Module** - Orchestrates the workflow between services

## Tech Stack

- **Language:** Java 17
- **Web Framework:** Javalin 6.1.3
- **JSON Processing:** Gson 2.11.0
- **Build Tool:** Maven 3.8+
- **Benchmarking:** JMH 1.37
- **Logging:** SLF4J 2.0.9

## Project Structure

```
stage-2/
├── ingestion_service/          # Downloads and stores books
│   ├── src/
│   └── pom.xml
├── indexing_service/           # Indexes books and extracts metadata
│   ├── src/
│   └── pom.xml
├── search_service/             # Handles search queries
│   ├── src/
│   └── pom.xml
├── controller/                 # Orchestrates the services
│   ├── src/
│   └── pom.xml
├── benchmark/                  # JMH benchmarks
│   ├── src/
│   └── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- At least 4GB RAM
- Internet connection (to download books)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/Microsoft-2/stage-2.git
cd stage-2
```

2. **Build all services**
```bash
mvn clean package
```

This will create JAR files in each service's `target/` directory.

### Running the System

You need to start the services in order:

1. **Start Ingestion Service**
```bash
java -jar ingestion_service/target/ingestion-1.0.0.jar
```

2. **Start Indexing Service** (in a new terminal)
```bash
java -jar indexing_service/target/indexing-1.0.0.jar
```

3. **Start Search Service** (in a new terminal)
```bash
java -jar search_service/target/search-1.0.0.jar
```

4. **Run Control Module** (in a new terminal)
```bash
java -jar controller/target/controller-1.0.0.jar
```

The services will be available at:
- Ingestion: `http://localhost:7001`
- Indexing: `http://localhost:7002`
- Search: `http://localhost:7003`

## API Usage

### Ingestion Service

**Download a book:**
```bash
curl -X POST http://localhost:7001/ingest/1342
```

**Check book status:**
```bash
curl http://localhost:7001/ingest/status/1342
```

**List all books:**
```bash
curl http://localhost:7001/ingest/list
```

### Indexing Service

**Index a specific book:**
```bash
curl -X POST http://localhost:7002/index/update/1342
```

**Rebuild entire index:**
```bash
curl -X POST http://localhost:7002/index/rebuild
```

**Get indexing stats:**
```bash
curl http://localhost:7002/index/status
```

### Search Service

**Simple search:**
```bash
curl "http://localhost:7003/search?q=adventure"
```

**Search with author filter:**
```bash
curl "http://localhost:7003/search?q=adventure&author=Jules%20Verne"
```

**Search with language filter:**
```bash
curl "http://localhost:7003/search?q=adventure&language=en"
```

**Search with year filter:**
```bash
curl "http://localhost:7003/search?q=adventure&year=1870"
```

**Combined filters:**
```bash
curl "http://localhost:7003/search?q=adventure&author=Jules%20Verne&language=fr&year=1870"
```

### Example Response

```json
{
  "query": "adventure",
  "filters": {
    "author": "Jules Verne",
    "language": "fr"
  },
  "count": 2,
  "results": [
    {
      "book_id": "103",
      "title": "Vingt mille lieues sous les mers",
      "author": "Jules Verne",
      "language": "fr",
      "year": 1870
    },
    {
      "book_id": "164",
      "title": "Voyage au centre de la Terre",
      "author": "Jules Verne",
      "language": "fr",
      "year": 1864
    }
  ]
}
```

## Configuration

Each service has an `application.properties` file in `src/main/resources/`:

**Ingestion Service:**
```properties
server.port=7001
datalake.root=/data/datalake
gutenberg.base.url=https://www.gutenberg.org/files
download.timeout.seconds=30
```

**Indexing Service:**
```properties
server.port=7002
datalake.root=/data/datalake
datamart.root=/data/datamarts
indexing.batch.size=100
```

**Search Service:**
```properties
server.port=7003
datamart.root=/data/datamarts
cache.enabled=true
cache.ttl.minutes=10
```

You can modify these to change ports, paths, or other settings.

## Running Benchmarks

We included JMH benchmarks to measure performance:

```bash
cd benchmark
mvn clean package
java -jar target/benchmarks.jar
```

This will run microbenchmarks for:
- Text tokenization
- Metadata extraction
- Index updates
- Search operations

Results are saved in JSON format for analysis.

## Performance Results

Based on our benchmarks (4 core CPU, 32GB RAM):

| Operation | Average Time | Throughput |
|-----------|-------------|------------|
| Book ingestion | 2.85 sec | 0.35 books/sec |
| Book indexing | 1.25 sec | 0.80 books/sec |
| Simple search | 3.2 ms | 312 queries/sec |
| Search with filters | 3.2 ms | 312 queries/sec |

**Load testing results:**
- Handles up to 50 concurrent users well (<60ms latency)
- Maximum throughput: ~857 requests/second
- Becomes CPU-bound at 100+ concurrent users

### Identified Bottlenecks

1. **Ingestion**: Network latency downloading from Project Gutenberg
2. **Indexing**: Text tokenization is the slowest operation
3. **Search**: CPU-bound when handling high concurrency
4. **Overall**: Single-instance deployment limits horizontal scaling

## Testing

Run unit tests for all services:

```bash
mvn test
```

Run tests for a specific service:

```bash
cd ingestion_service
mvn test
```

## Data Storage

### Datalake Structure
```
datalake/
└── YYYYMMDD/           # Date (20251104)
    └── HH/             # Hour (14)
        └── book_id/    # Book ID (1342)
            ├── header.txt
            └── body.txt
```

### Datamarts Structure
```
datamarts/
├── metadata/
│   └── books.csv           # Book metadata (title, author, year, language)
├── inverted_index/
│   └── index.dat           # Serialized inverted index
└── word_frequencies/
    └── frequencies.csv     # Term frequencies per book
```

## Known Issues & Limitations

- **Single instance only**: Can't scale horizontally without modifications
- **Local storage**: Datalake not shared between potential instances  
- **No load balancing**: Would need Nginx/HAProxy for multiple instances
- **Memory-bound**: Entire index must fit in RAM
- **No authentication**: APIs are open (suitable for academic environment only)

## Future Improvements

If we had more time, we'd add:

- **Docker**: Containerize all services
- **Load Balancer**: Nginx to distribute traffic
- **Message Queue**: RabbitMQ for async processing
- **Distributed Index**: Migrate to Elasticsearch
- **Cloud Storage**: Move datalake to S3/HDFS
- **Monitoring**: Prometheus + Grafana
- **Service Discovery**: Consul for dynamic registration

## Troubleshooting

**Services won't start:**
- Check if ports 7001-7003 are already in use
- Make sure Java 17+ is installed: `java -version`

**Can't download books:**
- Check internet connection
- Project Gutenberg might be temporarily down
- Try a different book ID

**Out of memory errors:**
- Increase JVM heap: `java -Xmx4G -jar service.jar`
- Reduce number of books being indexed

**Search returns no results:**
- Make sure indexing service has processed the books
- Check `/index/status` endpoint for indexing stats
- Verify books exist in datalake

## Documentation

For detailed information, see:
- **Full Report**: `docs/Stage2_Report.pdf` (includes architecture, benchmarks, analysis)
- **API Specification**: See "API Usage" section above
- **Architecture Diagrams**: Available in the report

## Contributing

Since this is a university project, we're not accepting external contributions. However, feel free to fork and use this as reference for your own projects!

## License

This is an academic project for the Big Data course at ULPGC. All rights reserved to the team members.

## Acknowledgments

- **Professor**: For guidance and project requirements
- **Project Gutenberg**: For providing free access to books
- **ULPGC**: Universidad de Las Palmas de Gran Canaria

## Contact

For questions about this project, contact any team member or check the course forum.

---

**Note**: This project was developed as part of the Big Data course requirements. The focus is on learning distributed systems concepts, not production deployment.
