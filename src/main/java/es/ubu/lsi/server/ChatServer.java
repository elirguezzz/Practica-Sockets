package es.ubu.lsi.server;

import java.io.*;
import java.net.*;
import java.util.*;
import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

/**
 * Servidor de chat que admite múltiples clientes y reenvía los mensajes.
 */
public class ChatServer {

    private static final int PORT = 1600;
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static Set<String> bannedUsers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);

                // Pedimos nombre de usuario
                out.println("Introduce tu nombre de usuario:");
                username = in.readLine();

                synchronized (clients) {
                    clients.add(this);
                }
                broadcast("Servidor: " + username + " se ha unido al chat.");

                String input;
                while ((input = in.readLine()) != null) {
                    System.out.println("Mensaje de " + username + ": " + input);

                    if (bannedUsers.contains(username)) {
                        out.println("Estas baneado, no puedes enviar mensajes.");
                        continue;
                    }

                    if (input.equalsIgnoreCase("/exit")) {
                        break;
                    }

                    // Comando para banear (temporal, solo para pruebas)
                    if (input.startsWith("/ban ")) {
                        String userToBan = input.split(" ")[1];
                        bannedUsers.add(userToBan);
                        broadcast("Servidor: " + userToBan + " ha sido baneado.");
                        continue;
                    }

                    if (input.startsWith("/unban ")) {
                        String userToUnban = input.split(" ")[1];
                        bannedUsers.remove(userToUnban);
                        broadcast("Servidor: " + userToUnban + " ha sido desbaneado.");
                        continue;
                    }

                    broadcast(username + ": " + input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.out.println(message);
                }
            }
        }

        private void disconnect() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (clients) {
                clients.remove(this);
            }
            broadcast("Servidor: " + username + " se ha desconectado.");
        }
    }
}
