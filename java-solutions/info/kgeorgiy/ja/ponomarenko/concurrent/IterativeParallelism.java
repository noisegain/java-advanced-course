package info.kgeorgiy.ja.ponomarenko.concurrent;


import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Class that implements {@link ScalarIP} interface.
 *
 * @author Ponomarenko Ilya
 */
public class IterativeParallelism implements ScalarIP {

    final private ParallelMapper mapper;

    /**
     * Creates a new instance of {@link IterativeParallelism}.
     * Uses dynamic {@link ParallelMapper} to perform calculations.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Creates a new instance of {@link IterativeParallelism}.
     *
     * @param mapper {@link ParallelMapper} to be used
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return new BinaryOperationHandler<T>(threads, Collections.unmodifiableList(values), (a, b) -> comparator.compare(a, b) >= 0 ? a : b).process();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return new FindFirstHandler<T>(threads, Collections.unmodifiableList(values), predicate).process() != null;
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return new CountHandler<T>(threads, Collections.unmodifiableList(values), predicate).process();
    }

    private class BinaryOperationHandler<T> extends CalculationHandler<T, T> {
        final BinaryOperator<T> operator;

        BinaryOperationHandler(int threadsCnt, List<T> values, BinaryOperator<T> operator) {
            super(threadsCnt, values);
            this.operator = operator;
        }

        @Override
        T calc(List<T> chunk) {
            return chunk.stream().reduce(operator).orElseThrow();
        }

        @Override
        T merge(List<T> results) {
            return results.stream().reduce(operator).orElseThrow();
        }

        @Override
        T defaultResult() throws NoSuchElementException {
            throw new NoSuchElementException();
        }
    }

    private class FindFirstHandler<T> extends CalculationHandler<T, T> {

        final Predicate<? super T> predicate;

        volatile boolean done = false;

        FindFirstHandler(int threadsCnt, List<T> values, Predicate<? super T> predicate) {
            super(threadsCnt, values);
            this.predicate = predicate;
        }

        @Override
        T calc(List<T> chunk) {
            for (T x : chunk) {
                if (done) {
                    return null;
                }
                if (predicate.test(x)) {
                    done = true;
                    return x;
                }
            }
            return null;
        }

        @Override
        T merge(List<T> results) {
            return results.stream().filter(Objects::nonNull).findFirst().orElse(null);
        }
    }

    private class CountHandler<T> extends CalculationHandler<T, Integer> {
        final Predicate<? super T> predicate;

        CountHandler(int threadsCnt, List<T> values, Predicate<? super T> predicate) {
            super(threadsCnt, values);
            this.predicate = predicate;
        }

        @Override
        Integer calc(List<T> chunk) {
            return (int) chunk.stream().filter(predicate).count();
        }

        @Override
        Integer merge(List<Integer> results) {
            return results.stream().mapToInt(Integer::intValue).sum();
        }

        @Override
        Integer defaultResult() {
            return 0;
        }
    }

    abstract protected class CalculationHandler<T, R> {
        final int threadsCnt;
        final List<T> values;

        CalculationHandler(int threadsCnt, List<T> values) {
            this.values = values;
            this.threadsCnt = Math.min(values.size(), threadsCnt);
        }

        abstract R calc(List<T> chunk);

        abstract R merge(List<R> results);

        R defaultResult() {
            return null;
        }

        R process() throws InterruptedException {
            if (values.isEmpty()) {
                return defaultResult();
            }
            final int k = values.size() / threadsCnt;
            final int mod = values.size() % threadsCnt;
            final List<List<T>> list = IntStream.range(0, threadsCnt).mapToObj((i) ->
                    values.subList(i * k + Math.min(i, mod), Math.min((i + 1) * k + Math.min(i + 1, mod), values.size()))).toList();
            final List<R> results;
            if (mapper != null) {
                results = mapper.map(this::calc, list);
            } else {
                try (ParallelMapperImpl curMapper = new ParallelMapperImpl(threadsCnt)) {
                    results = curMapper.map(this::calc, list);
                }
            }
            return merge(results);
        }
    }
}
