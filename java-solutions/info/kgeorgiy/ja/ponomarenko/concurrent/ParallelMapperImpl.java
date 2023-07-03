package info.kgeorgiy.ja.ponomarenko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Class that implements {@link ParallelMapper} interface.
 *
 * @author Ponomarenko Ilya
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final Thread[] pool;

    private final Queue<Runnable> tasks = new ArrayDeque<>();

    /**
     * Creates a new instance of {@link ParallelMapperImpl}.
     *
     * @param threads number of threads to use.
     *
     * @throws IllegalArgumentException if {@code threads <= 0}
     */
    public ParallelMapperImpl(int threads) throws IllegalArgumentException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }
        pool = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            pool[i] = new Thread(() -> {
                Runnable task;
                try {
                    while (!Thread.interrupted()) {
                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                tasks.wait();
                            }
                            task = tasks.poll();
                        }
                        task.run();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            });
            pool[i].start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final int size = args.size();
        Counter completed = new Counter();
        List<R> res = new ArrayList<>(Collections.nCopies(size, null));
        final RuntimeException[] exception = {null};
        IntStream.range(0, size).forEach(i -> {
            Runnable task = () -> {
                try {
                    res.set(i, f.apply(args.get(i)));
                } catch (RuntimeException e) {
                    synchronized (exception) {
                        if (exception[0] == null) {
                            exception[0] = e;
                        } else {
                            exception[0].addSuppressed(e);
                        }
                    }
                    System.err.println("Exception in thread " + Thread.currentThread().getName() + ": " + e.getMessage());
                }
                completed.inc();
                synchronized (completed) {
                    if (completed.count == size) {
                        completed.notify();
                    }
                }
            };
            synchronized (tasks) {
                tasks.add(task);
                tasks.notify();
            }
        });
        synchronized (completed) {
            while (completed.count < size) {
                completed.wait();
            }
        }
        if (exception[0] != null) {
            throw exception[0];
        }
        return res;
    }

    @Override
    public void close() {
        for (Thread thread : pool) {
            thread.interrupt();
        }
        boolean flag = false;
        for (Thread thread : pool) {
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting thread to join: " + e.getMessage());
                    flag = true;
                }
            }
        }
        if (flag) {
            Thread.currentThread().interrupt();
        }
    }

    private static class Counter {
        volatile int count = 0;

        synchronized void inc() {
            count++;
        }
    }
}
