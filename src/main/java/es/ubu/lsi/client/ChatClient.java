package es.ubu.lsi.client;

import java.io.*;
import java.net.*;

/**
 * Cliente de chat que se conecta al servidor, permite enviar y recibir mensajes.
 */
public class ChatClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1600;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // Hilo para recibir mensajes
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("*Conexión terminada*");
                }
            }).start();

            // Envío de mensajes al servidor
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.println(userMessage);
                if (userMessage.equalsIgnoreCase("/exit")) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
