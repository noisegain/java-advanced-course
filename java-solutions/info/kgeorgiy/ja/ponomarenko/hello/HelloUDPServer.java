package info.kgeorgiy.ja.ponomarenko.hello;

import info.kgeorgiy.ja.ponomarenko.base.Utils;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Implementation for {@link HelloServer} interface.
 *
 * @author Ponomarenko Ilya
 */
public class HelloUDPServer implements HelloServer {
    private static final String USAGE = "HelloUDPClient <port> <threads>";
    private ExecutorService executorService;
    private DatagramSocket socket;
    private int size;


    /**
     * Main method for {@link HelloUDPServer}.
     * Usage: {@code HelloUDPServer <port> <threads>}
     * <p>
     * {@code port} - port to send requests to
     * {@code threads} - number of threads to send requests from
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (!Utils.checkArgs(args, 2, USAGE) || !Utils.checkIntegers(args, 0, 1)) {
            return;
        }
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        try (HelloUDPServer server = new HelloUDPServer();
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            server.start(port, threads);
            while (!reader.readLine().equals("stop")) {
                System.out.println("Type 'stop' if you want to stop the server");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        executorService = Executors.newFixedThreadPool(threads);
        try {
            socket = new DatagramSocket(port);
            size = socket.getReceiveBufferSize();
            IntStream.range(0, threads).forEach(i -> executorService.execute(this::listen));
        } catch (SocketException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void listen() {
        DatagramPacket request = new DatagramPacket(new byte[size], size);
        while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                socket.receive(request);
                String response = "Hello, " + Requests.packetToString(request);
                Requests.send(socket, request.getSocketAddress(), response);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        socket.close();
        Utils.shutdown(executorService, 10, TimeUnit.MILLISECONDS);
    }
}
