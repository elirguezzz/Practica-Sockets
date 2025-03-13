package es.ubu.lsi.client;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost"; // Dirección del servidor
    private static final int SERVER_PORT = 1600; // Mismo puerto que el servidor

    public static void main(String[] args) {
            try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
            ) {
                System.out.println("*Conectado* escribe tu mensaje:");

                // Hilo para recibir mensajes del servidor
                new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            System.out.println("Servidor: " + serverMessage);
                        }
                    } catch (IOException e) {
                        System.out.println("Conexión cerrada.");
                    }
                }).start(); //Es start no run que no me daba cuenta porque no iba

                // Lee los mensajes del usuario y los envia al servidor
                String userMessage;
                while ((userMessage = userInput.readLine()) != null) {
                    out.println(userMessage);
                }

            } catch (IOException e) { //lanzo excepcion muy importante
                e.printStackTrace();
            }
        }
    }