package es.ubu.lsi.client;

import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * ChatClient es un cliente de chat que se conecta a un servidor, 
 * permite enviar y recibir mensajes, y soporta comandos como logout, ban y unban.
 * Este programa utiliza sockets para la comunicación en red.
 */
public class ChatClient {

    private static final String SERVER_ADDRESS = "localhost"; // Dirección del servidor
    private static final int SERVER_PORT = 1600; // Puerto del servidor

    /**
     * Método principal del cliente de chat.
     * 
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Conexión a través de sockets y creación de flujos de entrada/salida
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            // Solicita el nombre de usuario al cliente
            System.out.print("Introduce tu nombre de usuario: ");
            String username = scanner.nextLine();

            // Enviar el mensaje de conexión al servidor
            out.writeObject(new Message(MessageType.CONNECT, username, ""));
            out.flush();

            // Crear un hilo para recibir mensajes del servidor
            new Thread(() -> {
                try {
                    while (true) {
                        // Leer el mensaje recibido
                        Message msg = (Message) in.readObject();
                        System.out.println("<" + msg.getSender() + "> patrocina el mensaje: " + msg.getContent());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("* La conexión ha sido finalizada *");
                }
            }).start();

            // Bucle para enviar mensajes al servidor
            while (true) {
                String userInput = scanner.nextLine();

                // Comando para desconectarse
                if (userInput.equalsIgnoreCase("/logout")) {
                    out.writeObject(new Message(MessageType.DISCONNECT, username, ""));
                    out.flush();
                    break;

                // Comando para banear a otro usuario
                } else if (userInput.startsWith("/ban ")) {
                    String target = userInput.substring(5);
                    out.writeObject(new Message(MessageType.BAN, username, target));
                    out.flush();

                // Comando para quitar el ban a otro usuario
                } else if (userInput.startsWith("/unban ")) {
                    String target = userInput.substring(7);
                    out.writeObject(new Message(MessageType.UNBAN, username, target));
                    out.flush();

                // Enviar un mensaje normal
                } else {
                    out.writeObject(new Message(MessageType.MESSAGE, username, userInput));
                    out.flush();
                }
            }

        } catch (IOException e) {
            // Manejo de excepciones de entrada/salida
            e.printStackTrace();
        }
    }
}
