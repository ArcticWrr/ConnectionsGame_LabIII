package server.network;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationServiceUDP {
    private final Map<String, InetSocketAddress> udpClients = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    // Aggiunge o aggiorna un client nella rubrica
    public void registerClient(String username, String ip, int port) {
        udpClients.put(username, new InetSocketAddress(ip, port));
    }

    // Rimuove un client (es. al logout)
    public void unregisterClient(String username) {
        udpClients.remove(username);
    }

    // Invia un messaggio a tutti i client registrati
    public void broadcast(Object message) {
        byte[] data = gson.toJson(message).getBytes();

        try (DatagramSocket socket = new DatagramSocket()) {
            for (InetSocketAddress address : udpClients.values()) {
                DatagramPacket packet = new DatagramPacket(data, data.length, address);
                socket.send(packet);
            }
        } catch (IOException e) {
            System.err.println("Errore durante il broadcast UDP: " + e.getMessage());
        }
    }

    public void send(Object message, String username) {
        byte[] data = gson.toJson(message).getBytes();

        try (DatagramSocket socket = new DatagramSocket()) {
            InetSocketAddress address = udpClients.get(username);
            DatagramPacket packet = new DatagramPacket(data, data.length, address);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Errore durante il broadcast UDP: " + e.getMessage());
        }
    }
}
