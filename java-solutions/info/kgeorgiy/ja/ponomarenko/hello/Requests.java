package info.kgeorgiy.ja.ponomarenko.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Class for working with requests.
 *
 * @author Ponomarenko Ilya
 */
public class Requests {
    /**
     * Receives a packet from the socket.
     *
     * @param socket socket to receive from
     * @param size   size of the packet
     * @return received packet
     * @throws IOException if an I/O error occurs
     */
    public static DatagramPacket receive(DatagramSocket socket, int size) throws IOException {
        DatagramPacket packet = new DatagramPacket(new byte[size], size);
        socket.receive(packet);
        return packet;
    }

    /**
     * Converts a packet to a string.
     *
     * @param packet packet to convert
     * @return string representation of the packet
     */
    public static String packetToString(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    /**
     * Sends a request to the socket.
     *
     * @param socket        socket to send to
     * @param socketAddress address to send to
     * @param request       request to send
     * @throws IOException if an I/O error occurs
     */
    public static void send(DatagramSocket socket, SocketAddress socketAddress, String request) throws IOException {
        DatagramPacket packet = new DatagramPacket(request.getBytes(), request.length(), socketAddress);
        packet.setData(request.getBytes(StandardCharsets.UTF_8));
        socket.send(packet);
    }
}
