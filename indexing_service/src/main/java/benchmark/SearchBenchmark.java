package search_service.benchmark;

import org.openjdk.jmh.annotations.*;
import search_service.DatamartLoader;
import search_service.SearchController;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput) // Measure queries per second
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5) [cite: 333]
@Measurement(iterations = 10) [cite: 333]
@Fork(1)
@State(Scope.Benchmark)
public class SearchBenchmark {

    private DatamartLoader.Datamart datamart;
    
    // Pre-define the terms we will search for
    private String commonTerm = "the"; // Assuming 'the' is a very common word
    private String mediumTerm = "love"; // Assuming 'love' is moderately common
    private String rareTerm = "zyxwv";  // A term that is definitely not in the index

    @Setup(Level.Trial)
    public void setup() throws IOException {
        // Load the *entire* datamart once before all benchmarks.
        // This ensures we are benchmarking query speed, not load speed.
        // You MUST run the Indexing Service's /rebuild endpoint first!
        try {
            datamart = DatamartLoader.load();
        } catch (IOException e) {
            System.err.println("FATAL: Could not load datamart. " +
                               "Run the Indexing Service /rebuild endpoint first.");
            throw e;
        }
    }

    // Benchmark for index lookup (common word) [cite: 328]
    @Benchmark
    public Set<Integer> benchmarkSearchCommonTerm() {
        return SearchController.findBookIdsByQuery(datamart, commonTerm);
    }

    // Benchmark for index lookup (medium word)
    @Benchmark
    public Set<Integer> benchmarkSearchMediumTerm() {
        return SearchController.findBookIdsByQuery(datamart, mediumTerm);
    }

    // Benchmark for index lookup (missed query)
    @Benchmark
    public Set<Integer> benchmarkSearchRareTerm() {
        return SearchController.findBookIdsByQuery(datamart, rareTerm);
    }

    // Benchmark for a multi-term query
    @Benchmark
    public Set<Integer> benchmarkSearchMultiTerm() {
        return SearchController.findBookIdsByQuery(datamart, "pride and prejudice");
    }
}
