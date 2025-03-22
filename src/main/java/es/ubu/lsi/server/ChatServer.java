package es.ubu.lsi.server;

import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 1600;
    private static final Map<String, ObjectOutputStream> clients = new HashMap<>();
    private static final Map<String, Set<String>> bans = new HashMap<>(); // Quién banea a quién

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
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

                Message connectMsg = (Message) in.readObject();
                username = connectMsg.getSender();

                synchronized (clients) {
                    if (clients.containsKey(username)) {
                        out.writeObject(new Message(MessageType.MESSAGE, "Servidor", "El nombre ya está en uso"));
                        out.flush();
                        socket.close();
                        return;
                    }
                    clients.put(username, out);
                    bans.put(username, new HashSet<>());
                }

                broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " se ha conectado"));

                Message msg;
                while ((msg = (Message) in.readObject()) != null) {
                	switch (msg.getType()) {
                    case MESSAGE:
                        handleChatMessage(msg);
                        break;
                    case DISCONNECT:
                        disconnect();
                        return;
                    case BAN:
                        handleBan(msg);
                        break;
                    case UNBAN:
                        handleUnban(msg);
                        break;
                    case CONNECT:                    
                        break;
                }

                }
            } catch (IOException | ClassNotFoundException e) {
                disconnect();
            }
        }

        private void handleChatMessage(Message msg) {
            synchronized (clients) {
                for (Map.Entry<String, ObjectOutputStream> entry : clients.entrySet()) {
                    String receiver = entry.getKey();
                    ObjectOutputStream clientOut = entry.getValue();

                    // Si el receptor ha baneado al emisor, no le llega el mensaje
                    if (!bans.get(receiver).contains(msg.getSender())) {
                        try {
                            clientOut.writeObject(msg);
                            clientOut.flush();
                        } catch (IOException ignored) {}
                    }
                }
            }
        }

        private void handleBan(Message msg) {
            String target = msg.getContent();
            bans.get(username).add(target);
            broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " ha baneado a " + target));
        }

        private void handleUnban(Message msg) {
            String target = msg.getContent();
            bans.get(username).remove(target);
            broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " ha desbloqueado a " + target));
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

        private void disconnect() {
            if (username != null) {
                synchronized (clients) {
                    clients.remove(username);
                    bans.remove(username);
                }
                broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " se ha desconectado"));
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
