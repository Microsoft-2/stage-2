package benchmark;

import indexing_service.IndexBuilder;
import org.openjdk.jmh.annotations.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Configure the benchmark settings
@BenchmarkMode(Mode.Throughput) // Measure operations per second
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5) [cite: 333] // 5 warmup runs
@Measurement(iterations = 10) [cite: 333] // 10 real measurement runs
@Fork(1) // Run in a single process
@State(Scope.Benchmark) // Use one instance of this state for all threads
public class IndexingBenchmark {

    private Path sampleBodyPath;
    private Path sampleHeaderPath;
    
    // We use Level.Invocation to get a fresh, empty map for every
    // single method call. This is crucial for benchmarking "updates".
    @State(Scope.Invocation)
    public static class InvocationState {
        public Map<String, Map<Integer, Integer>> index = new HashMap<>();
        public Map<Integer, BookMetadata> metadata = new HashMap<>();
    }

    // @Setup runs before the benchmark.
    // We use it to prepare sample data so we're not benchmarking IO.
    @Setup(Level.Trial)
    public void setup() throws IOException {
        // Create temporary sample files
        sampleBodyPath = Files.createTempFile("jmh_body", ".txt");
        String bodyContent = "The quick brown fox jumps over the lazy dog. " +
                             "A fox is a fox, and a dog is a dog.";
        Files.writeString(sampleBodyPath, bodyContent);

        sampleHeaderPath = Files.createTempFile("jmh_header", ".txt");
        // A minimal mock of a Gutendex header
        String headerContent = "{" +
            "\"id\": 123," +
            "\"title\": \"Sample Book\"," +
            "\"authors\": [{\"name\": \"JMH Tester\"}]," +
            "\"languages\": [\"en\"]," +
            "\"publication_year\": 2025" +
            "}";
        Files.writeString(sampleHeaderPath, headerContent);
    }

    // @TearDown runs after the benchmark to clean up.
    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        Files.delete(sampleBodyPath);
        Files.delete(sampleHeaderPath);
    }

    // This is the benchmark for text tokenization and index update [cite: 326, 328]
    @Benchmark
    public void benchmarkProcessBody(InvocationState state) {
        // We pass the invocation-scoped state to measure the "add" operation
        IndexBuilder.processBodyFile(sampleBodyPath, state.index);
    }

    // This is the benchmark for metadata parsing and insertion [cite: 327]
    @Benchmark
    public void benchmarkProcessHeader(InvocationState state) {
        // We pass the invocation-scoped state to measure the "add" operation
        IndexBuilder.processHeaderFile(sampleHeaderPath, state.metadata);
    }
}
