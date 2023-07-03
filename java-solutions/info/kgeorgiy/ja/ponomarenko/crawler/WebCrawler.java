package info.kgeorgiy.ja.ponomarenko.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static info.kgeorgiy.ja.ponomarenko.base.Utils.checkArgs;
import static info.kgeorgiy.ja.ponomarenko.base.Utils.parseOrDefault;

/**
 * Class that implements {@link Crawler} interface.
 *
 * @author Ponomarenko Ilya
 */
public class WebCrawler implements Crawler {
    private static final String USAGE = "WebCrawler url [depth [downloads [extractors [perHost]]]]";
    private final ExecutorService downloadExecutor;
    private final ExecutorService extractExecutor;
    private final Downloader downloader;

    /**
     * Creates a new instance of {@link WebCrawler}.
     *
     * @param downloader  {@link Downloader} to use
     * @param downloaders number of downloaders
     * @param extractors  number of extractors
     * @param perHost     maximum number of downloads from one host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        downloadExecutor = Executors.newFixedThreadPool(downloaders);
        extractExecutor = Executors.newFixedThreadPool(extractors);
        this.downloader = downloader;
    }

    /**
     * Main method for {@link WebCrawler}.
     * Usage: {@code WebCrawler url [depth [downloads [extractors]]]}
     *
     * {@code url} - starting url
     * {@code depth} - depth of crawling
     * {@code downloads} - number of downloaders
     * {@code extractors} - number of extractors
     * all arguments are optional
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (!checkArgs(args, 1, 5, USAGE)) {
            return;
        }
        final String url = args[0];
        try {
            final int depth = parseOrDefault(args, 1, 1, "depth");
            final int downloaders = parseOrDefault(args, 2, 1, "downloaders");
            final int extractors = parseOrDefault(args, 3, 1, "extractors");
            final int perHost = parseOrDefault(args, 4, 1, "perHost");
            try (Crawler crawler = new WebCrawler(new CachingDownloader(0), downloaders, extractors, perHost)) {
                Result result = crawler.download(url, depth);
                System.out.println(result.getDownloaded().stream().collect(Collectors.joining(System.lineSeparator())));
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Result download(String url, int depth) {
        return new DownloadTask(url, depth).call();
    }

    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(150, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void close() {
        shutdown(downloadExecutor);
        shutdown(extractExecutor);
    }

    private class DownloadTask implements Callable<Result> {
        private final String url;
        private final int depth;
        private final Set<String> downloaded = ConcurrentHashMap.newKeySet();
        private final Map<String, IOException> errors = new ConcurrentHashMap<>();
        private Set<String> curLayer = ConcurrentHashMap.newKeySet();
        private Set<String> nextLayer = ConcurrentHashMap.newKeySet();

        public DownloadTask(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }

        public Result call() {
            curLayer.add(url);
            download(depth);
            return new Result(downloaded.stream().filter(s -> !errors.containsKey(s)).collect(Collectors.toList()), errors);
        }

        private void download(int depth) {
            final Phaser phaser = new Phaser(1);
            IntStream.range(0, depth).forEach(i -> {
                curLayer.forEach(x -> {
                    phaser.register();
                    downloadExecutor.execute(downloadTask(depth, i, phaser, x));
                });
                phaser.arriveAndAwaitAdvance();
                curLayer = nextLayer;
                nextLayer = ConcurrentHashMap.newKeySet();
            });
        }

        private Runnable downloadTask(int depth, int i, Phaser phaser, String x) {
            return () -> {
                try {
                    if (downloaded.add(x)) {
                        final Document res = downloader.download(x);
                        if (i + 1 < depth) {
                            phaser.register();
                            extractExecutor.execute(executeTask(phaser, x, res));
                        }
                    }
                } catch (IOException e) {
                    errors.put(x, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            };
        }

        private Runnable executeTask(Phaser phaser, String x, Document res) {
            return () -> {
                try {
                    nextLayer.addAll(res.extractLinks());
                } catch (IOException e) {
                    errors.put(x, e);
                } finally {
                    phaser.arriveAndDeregister();
                }
            };
        }
    }
}
