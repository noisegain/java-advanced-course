package info.kgeorgiy.ja.ponomarenko.hello;

import info.kgeorgiy.ja.ponomarenko.base.Utils;
import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


/**
 * Implementation for {@link HelloClient} interface.
 *
 * @author Ilya Ponomarenko
 */
public class HelloUDPClient implements HelloClient {
    public static final long REQUEST_FACT = 5L;
    private static final String USAGE = "HelloUDPClient <host> <port> <prefix> <threads> <requests>";

    /**
     * Main method for {@link HelloUDPClient}.
     * Usage: {@code HelloUDPClient <host> <port> <prefix> <threads> <requests>}
     * <p>
     * {@code host} - host to send requests to
     * {@code port} - port to send requests to
     * {@code prefix} - prefix of requests
     * {@code threads} - number of threads to send requests from
     * {@code requests} - number of requests to send from each thread
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (!Utils.checkArgs(args, 5, USAGE) || !Utils.checkIntegers(args, 1, 3, 4)) {
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int threads = Integer.parseInt(args[3]);
        int requests = Integer.parseInt(args[4]);
        new HelloUDPClient().run(host, port, prefix, threads, requests);
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.err.println("Error: unknown host " + host);
            return;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        for (int i = 1; i <= threads; i++) {
            executorService.submit(new DoRequest(i, requests, prefix, address, port));
        }
        Utils.shutdown(executorService, requests * REQUEST_FACT, TimeUnit.SECONDS);
    }

    private static class DoRequest implements Runnable {
        private static final int TIMEOUT = 44;
        final int i;
        final int requests;
        final int port;
        final String prefix;
        private final SocketAddress socketAddress;
        private int size;
        private DatagramSocket socket;

        public DoRequest(int i, int requests, String prefix, InetAddress address, int port) {
            this.i = i;
            this.requests = requests;
            this.prefix = prefix;
            this.port = port;
            socketAddress = new InetSocketAddress(address, port);
        }

        @Override
        public void run() {
            try (var datagramSocket = new DatagramSocket()) {
                socket = datagramSocket;
                socket.setSoTimeout(TIMEOUT);
                size = socket.getReceiveBufferSize();
                IntStream.range(1, requests + 1).mapToObj(j -> prefix + i + "_" + j).forEach(this::sendAndReceive);
            } catch (SocketException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        private void sendAndReceive(String request) {
            while (!socket.isClosed()) {
                try {
                    Requests.send(socket, socketAddress, request);
                    String response = Requests.packetToString(Requests.receive(socket, size));
                    if (response.equals("Hello, " + request)) {
                        System.out.println("Received: " + response);
                        break;
                    }
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
