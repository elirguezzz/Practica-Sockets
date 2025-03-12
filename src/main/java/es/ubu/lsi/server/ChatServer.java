package es.ubu.lsi.server;
/**
* Clase que abre un servidor socket en el puerto 1500, maneja multiples clientes 
* usando una lista y recibe los mensajes de los clientes y los reenvia a todos.
* 
* @author <a href="erd1005@alu.ubu.es"> Elisa Rodríguez Domínguez </a>
* @since 1.0
* @version 1.0
*/
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	
	// Puerto del servidor, voy a usar 1600 porque el 1500 lo tengo en uso y no quiero quitarlo.
	private static final int PORT = 1600;
	
	// Lista de clientes conectados
    private static List<PrintWriter> clients = new ArrayList<>(); 

    public static void main(String[] args) {
        System.out.println("Servidor iniciado en el puerto " + PORT);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start(); // Acepta conexiones
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clients) {
                    clients.add(out);
                }
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Mensaje recibido: " + message);
                    broadcast(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (clients) {
                    clients.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clients) {
                for (PrintWriter client : clients) {
                    client.println(message);
                }
            }
        }
    }
}
