package info.kgeorgiy.ja.ponomarenko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ListIP} interface.
 * Uses {@link IterativeParallelism} as a base class.
 *
 * @author Ponomarenko Ilya
 */
public class IterativeParallelismHard extends IterativeParallelism implements ListIP {
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return new JoinHandler<>(threads, values).process();
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return new FilterHandler<T>(threads, Collections.unmodifiableList(values), predicate).process();
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return Collections.unmodifiableList(new MapHandler<>(threads, values, f).process());
    }

    private class JoinHandler<T> extends CalculationHandler<T, String> {
        JoinHandler(int threadsCnt, List<T> values) {
            super(threadsCnt, values);
        }

        @Override
        String calc(List<T> chunk) {
            return chunk.stream().map(Object::toString).collect(Collectors.joining());
        }

        @Override
        String merge(List<String> results) {
            return String.join("", results);
        }

        @Override
        String defaultResult() {
            return "";
        }
    }

    private abstract class ListOutputHandler<T, R> extends CalculationHandler<T, List<R>> {

        ListOutputHandler(int threadsCnt, List<T> values) {
            super(threadsCnt, values);
        }

        @Override
        List<R> merge(List<List<R>> results) {
            return results.stream().flatMap(List::stream).toList();
        }

        @Override
        List<R> defaultResult() {
            return List.of();
        }
    }

    private class FilterHandler<T> extends ListOutputHandler<T, T> {

        private final Predicate<? super T> predicate;

        FilterHandler(int threadsCnt, List<T> values, Predicate<? super T> predicate) {
            super(threadsCnt, values);
            this.predicate = predicate;
        }

        @Override
        List<T> calc(List<T> chunk) {
            return chunk.stream().filter(predicate).toList();
        }
    }

    private class MapHandler<T, U> extends ListOutputHandler<T, U> {

        private final Function<? super T, ? extends U> f;

        MapHandler(int threadsCnt, List<T> values, Function<? super T, ? extends U> f) {
            super(threadsCnt, values);
            this.f = f;
        }

        @Override
        List<U> calc(List<T> chunk) {
            return chunk.stream().map(f).collect(Collectors.toUnmodifiableList());
        }
    }
}
