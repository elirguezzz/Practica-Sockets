package es.ubu.lsi.client;

import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 1600;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.print("Introduce tu nombre de usuario: ");
            String username = scanner.nextLine();

            // Enviamos el mensaje de conexión
            out.writeObject(new Message(MessageType.CONNECT, username, ""));
            out.flush();

            // Hilo para recibir mensajes
            new Thread(() -> {
                try {
                    while (true) {
                        Message msg = (Message) in.readObject();
                        System.out.println("<" + msg.getSender() + "> patrocina el mensaje: " + msg.getContent());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("* La conexion ha sido finalizada *");
                }
            }).start();

            // Bucle para enviar mensajes
            while (true) {
                String userInput = scanner.nextLine();

                if (userInput.equalsIgnoreCase("/logout")) {
                    out.writeObject(new Message(MessageType.DISCONNECT, username, ""));
                    out.flush();
                    break;
                } else if (userInput.startsWith("/ban ")) {
                    String target = userInput.substring(5);
                    out.writeObject(new Message(MessageType.BAN, username, target));
                    out.flush();
                } else if (userInput.startsWith("/unban ")) {
                    String target = userInput.substring(7);
                    out.writeObject(new Message(MessageType.UNBAN, username, target));
                    out.flush();
                } else {
                    out.writeObject(new Message(MessageType.MESSAGE, username, userInput));
                    out.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
