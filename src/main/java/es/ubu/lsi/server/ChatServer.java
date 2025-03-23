package es.ubu.lsi.server;

import es.ubu.lsi.common.Message;
import es.ubu.lsi.common.MessageType;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * La clase ChatServer implementa un servidor de chat multicliente
 * que maneja la conexión de usuarios, transmisión de mensajes, y comandos 
 * como banear o desbloquear usuarios.
 * Utiliza sockets para la comunicación en red y gestiona a los clientes de forma concurrente.
 */
public class ChatServer {

    /**
     * Puerto en el que se ejecuta el servidor.
     */
    private static final int PORT = 1600;

    /**
     * Mapa que almacena el nombre de los clientes conectados junto con 
     * sus respectivos flujos de salida para enviar mensajes.
     */
    private static final Map<String, ObjectOutputStream> clients = new HashMap<>();

    /**
     * Mapa que gestiona a qué usuarios ha bloqueado (baneado) cada usuario.
     */
    private static final Map<String, Set<String>> bans = new HashMap<>();

    /**
     * Método principal que inicia el servidor y gestiona las conexiones entrantes.
     * 
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start(); // Manejar cada cliente en un hilo separado
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clase interna que maneja las interacciones con un cliente de manera individual.
     */
    static class ClientHandler extends Thread {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String username;

        /**
         * Constructor que inicializa el manejador de cliente con el socket asociado.
         * 
         * @param socket El socket de conexión del cliente.
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Método principal del hilo, responsable de gestionar la interacción cliente-servidor.
         */
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                // Leer y registrar el mensaje de conexión del cliente
                Message connectMsg = (Message) in.readObject();
                username = connectMsg.getSender();

                synchronized (clients) {
                    // Evitar nombres duplicados
                    if (clients.containsKey(username)) {
                        out.writeObject(new Message(MessageType.MESSAGE, "Servidor", "El nombre ya está en uso"));
                        out.flush();
                        socket.close();
                        return;
                    }
                    // Registrar cliente y sus bloqueos
                    clients.put(username, out);
                    bans.put(username, new HashSet<>());
                }

                // Anunciar la conexión del cliente a todos los demás
                broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " se ha conectado"));

                Message msg;
                // Procesar mensajes del cliente
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
                        case CONNECT: // Ya procesado en este punto
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                disconnect(); // Manejar la desconexión del cliente
            }
        }

        /**
         * Envía un mensaje de chat a todos los clientes excepto a aquellos bloqueados.
         * 
         * @param msg El mensaje a enviar.
         */
        private void handleChatMessage(Message msg) {
            synchronized (clients) {
                for (Map.Entry<String, ObjectOutputStream> entry : clients.entrySet()) {
                    String receiver = entry.getKey();
                    ObjectOutputStream clientOut = entry.getValue();

                    // Evitar enviar mensajes a usuarios que han bloqueado al emisor
                    if (!bans.get(receiver).contains(msg.getSender())) {
                        try {
                            clientOut.writeObject(msg);
                            clientOut.flush();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }

        /**
         * Maneja la acción de bloquear (banear) a un usuario.
         * 
         * @param msg Mensaje que contiene el nombre del usuario a bloquear.
         */
        private void handleBan(Message msg) {
            String target = msg.getContent();
            bans.get(username).add(target);
            broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " ha baneado a " + target));
        }

        /**
         * Maneja la acción de desbloquear a un usuario.
         * 
         * @param msg Mensaje que contiene el nombre del usuario a desbloquear.
         */
        private void handleUnban(Message msg) {
            String target = msg.getContent();
            bans.get(username).remove(target);
            broadcast(new Message(MessageType.MESSAGE, "Servidor", username + " ha desbloqueado a " + target));
        }

        /**
         * Envía un mensaje a todos los clientes conectados.
         * 
         * @param message El mensaje a enviar.
         */
        private void broadcast(Message message) {
            synchronized (clients) {
                for (ObjectOutputStream clientOut : clients.values()) {
                    try {
                        clientOut.writeObject(message);
                        clientOut.flush();
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        /**
         * Desconecta al cliente y notifica a los demás.
         */
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
            } catch (IOException ignored) {
            }
        }
    }
}
