package es.ubu.lsi.server;

import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1600;
    private static final Map<String, ObjectOutputStream> clients = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Leer el primer mensaje (conexión)
                Message message = (Message) in.readObject();
                if (message.getType() == MessageType.CONNECT) {
                    username = message.getSender();

                    synchronized (clients) {
                        if (clients.containsKey(username)) {
                            out.writeObject(new Message(MessageType.MESSAGE, "Servidor", "El nombre ya está en uso."));
                            out.flush();
                            socket.close();
                            return;
                        }
                        clients.put(username, out);
                    }

                    System.out.println(username + " se ha conectado.");
                    broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " se ha unido al chat."));
                }

                // Manejar mensajes del usuario
                while (true) {
                    message = (Message) in.readObject();
                    if (message.getType() == MessageType.DISCONNECT) {
                        break;
                    }
                    broadcast(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(username + " se ha desconectado inesperadamente.");
            } finally {
                disconnectUser();
            }
        }

        private void broadcast(Message message) {
            synchronized (clients) {
                for (ObjectOutputStream clientOut : clients.values()) {
                    try {
                        clientOut.writeObject(message);
                        clientOut.flush();
                    } catch (IOException ignored) {}
                }
            }
        }

        private void disconnectUser() {
            if (username != null) {
                synchronized (clients) {
                    clients.remove(username);
                }
                System.out.println(username + " se ha desconectado.");
                broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " ha salido del chat."));
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
