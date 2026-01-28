package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class udpListener implements Runnable{
    private final DatagramSocket udpSocket;

    public udpListener(DatagramSocket udpSocket){
        this.udpSocket = udpSocket;
    }

    @Override
    public void run(){

        byte[] buffer = new byte[1024];
        System.out.println("[SISTEMA] Ascolto notifiche UDP sulla porta: " + udpSocket.getLocalPort());

        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("\n[NOTIFICA DAL SERVER]: " + message);
                System.out.print("> "); // Ristampiamo il cursore per l'utente
            } catch (IOException e) {
                if (!udpSocket.isClosed()) {
                    System.err.println("Errore nel listener UDP: " + e.getMessage());
                }
                break;
            }
        }
    }
}
