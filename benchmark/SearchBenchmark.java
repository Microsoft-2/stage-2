package benchmark;

import org.openjdk.jmh.annotations.*;
import search_service.DatamartLoader;
import search_service.SearchController;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5) // [cite: 333]
@Measurement(iterations = 10) // [cite: 333]
@Fork(1)
@State(Scope.Benchmark)
public class SearchBenchmark {

    private DatamartLoader.Datamart datamart;

    private static final String COMMON_TERM = "the";
    private static final String MEDIUM_TERM = "love";
    private static final String RARE_TERM = "zyxwv";

    @Setup(Level.Trial)
    public void setup() throws IOException {
        try {
            datamart = DatamartLoader.load();

            if (datamart == null) {
                throw new IllegalStateException("Datamart is null after loading");
            }

        } catch (IOException e) {
            System.err.println("FATAL: Could not load datamart. " +
                    "Run the Indexing Service /rebuild endpoint first.");
            throw e;
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        datamart = null; // liberar referencia
    }

    // Benchmark for index lookup (common word) [cite: 328]
    @Benchmark
    public Set<Integer> benchmarkSearchCommonTerm() {
        return SearchController.findBookIdsByQuery(datamart, COMMON_TERM);
    }

    @Benchmark
    public Set<Integer> benchmarkSearchMediumTerm() {
        return SearchController.findBookIdsByQuery(datamart, MEDIUM_TERM);
    }

    @Benchmark
    public Set<Integer> benchmarkSearchRareTerm() {
        return SearchController.findBookIdsByQuery(datamart, RARE_TERM);
    }

    @Benchmark
    public Set<Integer> benchmarkSearchMultiTerm() {
        return SearchController.findBookIdsByQuery(datamart, "pride and prejudice");
    }
}
