package benchmark;

import indexing_service.IndexBuilder;
import org.openjdk.jmh.annotations.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5) [cite: 333]
@Measurement(iterations = 10) [cite: 333]
@Fork(1)
@State(Scope.Benchmark)
public class IndexingBenchmark {

    private Path sampleBodyPath;
    private Path sampleHeaderPath;

    @State(Scope.Invocation)
    public static class InvocationState {
        public Map<String, Map<Integer, Integer>> index = new HashMap<>();
        public Map<Integer, BookMetadata> metadata = new HashMap<>();
    }

    @Setup(Level.Trial)
    public void setup() throws IOException {
        sampleBodyPath = Files.createTempFile("jmh_body", ".txt");
        String bodyContent = "The quick brown fox jumps over the lazy dog. " +
                             "A fox is a fox, and a dog is a dog.";
        Files.writeString(sampleBodyPath, bodyContent);

        sampleHeaderPath = Files.createTempFile("jmh_header", ".txt");
        String headerContent = "{" +
            "\"id\": 123," +
            "\"title\": \"Sample Book\"," +
            "\"authors\": [{\"name\": \"JMH Tester\"}]," +
            "\"languages\": [\"en\"]," +
            "\"publication_year\": 2025" +
            "}";
        Files.writeString(sampleHeaderPath, headerContent);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws IOException {
        Files.delete(sampleBodyPath);
        Files.delete(sampleHeaderPath);
    }

    @Benchmark
    public void benchmarkProcessBody(InvocationState state) {
        IndexBuilder.processBodyFile(sampleBodyPath, state.index);
    }
    @Benchmark
    public void benchmarkProcessHeader(InvocationState state) {
        IndexBuilder.processHeaderFile(sampleHeaderPath, state.metadata);
    }
}
