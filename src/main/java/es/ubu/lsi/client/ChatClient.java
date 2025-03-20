package es.ubu.lsi.client;

import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost"; // Dirección del servidor
    private static final int SERVER_PORT = 1600; // Puerto del servidor

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.print("Ingresa tu nombre de usuario: ");
            String username = scanner.nextLine();

            // Enviar mensaje de conexión al servidor
            out.writeObject(new Message(MessageType.CONNECT, username, "Se ha conectado"));
            out.flush();

            // Hilo para recibir mensajes del servidor
            new Thread(() -> {
                try {
                    while (true) {
                        Message message = (Message) in.readObject();
                        System.out.println(message);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("*Conexión terminada*");
                }
            }).start();

            // Enviar mensajes al servidor
            while (true) {
                String userMessage = scanner.nextLine();
                if (userMessage.equalsIgnoreCase("logout")) {
                    out.writeObject(new Message(MessageType.DISCONNECT, username, "Se ha desconectado"));
                    out.flush();
                    break;
                } else {
                    out.writeObject(new Message(MessageType.MESSAGE, username, userMessage));
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
