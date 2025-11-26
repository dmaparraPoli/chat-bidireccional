/*
 * Clase: Client
 * Descripción: Implementa el cliente del sistema de chat bidireccional basado en sockets.
 * El cliente se conecta al servidor, envía mensajes y recibe respuestas en tiempo real.
 * Permite enviar comandos como "/chao" para finalizar la conexión.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    // Socket para conectarse al servidor
    private Socket client;

    // Flujos de entrada/salida para la comunicación con el servidor
    private BufferedReader in;   // Recibe mensajes desde el servidor
    private PrintWriter out;     // Envía mensajes al servidor

    // Bandera para controlar el estado del cliente
    private boolean done;

    /**
     * Método principal de ejecución del cliente.
     * - Establece la conexión con el servidor.
     * - Inicia un hilo adicional para leer mensajes desde la consola.
     * - Escucha continuamente los mensajes enviados desde el servidor.
     */
    @Override
    public void run() {
        try {
            // Establece la conexión con el servidor (localhost, puerto 65432)
            client = new Socket("127.0.0.1", 65432);

            // Inicializa los flujos de entrada/salida
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // Crea un hilo separado para manejar la entrada del usuario desde consola
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            // Bucle para recibir mensajes del servidor y mostrarlos en consola
            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (IOException e) {
            // Si ocurre un error en la conexión, se cierra el cliente
            shutdown();
        }
    }

    /**
     * Método shutdown().
     * Finaliza la conexión del cliente con el servidor y libera los recursos.
     */
    public void shutdown() {
        done = true;
        try {
            // Cierra los flujos y el socket
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (Exception e) {
            // Error ignorado intencionalmente
        }
    }

    /**
     * Clase interna: InputHandler
     * Descripción:
     * - Gestiona la entrada de texto desde el teclado del usuario.
     * - Envía los mensajes al servidor a través del flujo de salida.
     * - Permite al usuario cerrar la conexión escribiendo "/chao".
     */
    class InputHandler implements Runnable {

        /**
         * Método run() del hilo de entrada del usuario.
         * Lee continuamente desde la consola y envía los mensajes al servidor.
         */
        @Override
        public void run() {
            try {
                // Flujo para leer datos ingresados por el usuario desde el teclado
                BufferedReader InReader = new BufferedReader(new InputStreamReader(System.in));

                // Bucle que permanece activo mientras el cliente esté conectado
                while (!done) {
                    String message = InReader.readLine();

                    // Si el usuario escribe "/chao", se desconecta
                    if (message.equals("/chao")) {
                        out.println(message); // Informa al servidor
                        InReader.close();     // Cierra el lector de consola
                        shutdown();           // Cierra la conexión del cliente
                    } else {
                        // Envia el mensaje al servidor
                        out.println(message);
                    }
                }

            } catch (IOException e) {
                // En caso de error de E/S, se finaliza la conexión
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}