# Stage 2

##  Overview

**Stage 2** is a modular **Java** project designed to implement a system for **data ingestion, indexing, and search**.  
Its goal is to provide a scalable, decoupled architecture capable of processing large data volumes and delivering efficient search capabilities.
---

##  Project Architecture

The repository is organized into several main modules:

| Folder | Description |
|---------|--------------|
| `ingestion_service/` | Handles **data ingestion** from different sources. |
| `indexing_service/` | Responsible for **indexing data** to enable fast searches. |
| `search_service/` | Provides the **search functionality** over the indexed data. |
| `controller/` | Contains controllers or coordination logic between services. |
| `benchmark/` | Contains **performance and load testing** scripts. |
| `benchmarkUNPACKAGED/` | Unpackaged or experimental versions of benchmark tests. |

---

##  Installation & Execution

Prerequisites
 - Java 17+ (or the required version)
 - Maven or Gradle
 - Operating System: Linux, macOS, or Windows

 1. Clone the repository

```bash
git clone https://github.com/Microsoft-2/stage-2.git
cd stage-2
```

 2. Build the project
```bash
mvn clean install
```

### Running the Applications
 To start the system, run the applications in the following order:
> 1. Start the ingestion_service
> 2. Start the indexing_service
> 3. Run the controller to coordinate the services


##  Benchmarks

The repository includes two folders dedicated to performance testing:  

These modules are intended to evaluate:
- Data ingestion throughput
- Indexing speed
- Search latency
- Overall scalability

---

##  Configuration

Configuration parameters such as ports, file paths, and data sources are typically defined in `.properties` or `.yaml` files inside each module.

Example:

```properties
server.port=8080
index.path=/data/index
ingestion.source=/data/input
```

##  Basic Usage

Once the system is up and running, you can:

1. **Ingest data**
2. **Index the ingested data**
3. **Perform searches** via API or CLI tools

Example API call (if a REST interface is provided):

```bash
curl -X GET "http://localhost:8080/search?q=example"
```

##  Authors & Contributors

This project was developed by multiple contributors (as listed in the commit history).

| Name |
|------|
| *Enrique Padrón*| 
| *Jorge Curbero* | 
| *Daniel Medina* | 
| *Sergio Muela* |


---

##  Repository Information

- **Repository:** [Microsoft-2/stage-2](https://github.com/Microsoft-2/stage-2)  
- **Language:** `Java (100%)`  
- **Commits:** `11`  
- **Contributors:** `4`  
- **Default branch:** `main`  
- **Folders:**  
  - `benchmark/`  
  - `controller/`  
  - `ingestion_service/`  
  - `indexing_service/`  
  - `search_service/`
