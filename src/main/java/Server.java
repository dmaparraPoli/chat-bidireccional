/*
 * Clase: Server
 * Descripción: Implementa un servidor de chat bidireccional utilizando sockets
 * Cada cliente que se conecta se maneja en un hilo separado, permitiendo comunicación
 * simultánea entre múltiples usuarios
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    // Lista de todos los clientes conectados (manejadores de conexión)
    private final ArrayList<ConnectionHandler> connections;

    // Socket principal del servidor
    private ServerSocket server;

    // Bandera para controlar el ciclo de ejecución
    private Boolean done;

    // Pool de hilos para manejar múltiples clientes de forma concurrente
    private ExecutorService pool;

    /**
     * Constructor de la clase Server
     * Inicializa la lista de conexiones y establece el estado inicial del servidor
     */
    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    /**
     * Método run() que ejecuta el servidor
     * - Crea el ServerSocket en el puerto 65432
     * - Espera conexiones entrantes de clientes
     * - Por cada conexión, crea un hilo (ConnectionHandler) que gestiona la comunicación
     */
    @Override
    public void run() {
        try {
            server = new ServerSocket(65432); // Puerto de escucha del servidor
            pool = Executors.newCachedThreadPool(); // Crea un pool dinámico de hilos

            // Bucle principal que acepta clientes hasta que done sea true
            while (!done) {
                Socket client = server.accept(); // Espera conexión de un cliente
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler); // Agrega el cliente a la lista de conexiones
                pool.execute(handler);    // Ejecuta el hilo del cliente
            }
        } catch (IOException e) {
            shutdown(); // Si ocurre un error, se cierra el servidor
        }
    }

    /**
     * Método shutdown()
     * Cierra el servidor y todas las conexiones activas de forma ordenada
     */
    public void shutdown() {
        try {
            done = true; // Marca el servidor como detenido
            pool.shutdown(); // Detiene el pool de hilos

            // Cierra el socket principal del servidor si está abierto
            if (!server.isClosed()) {
                server.close();
            }

            // Cierra todas las conexiones con los clientes
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // Error ignorado intencionalmente (ya se está cerrando)
        }
    }

    /**
     * Clase interna: ConnectionHandler
     * Se encarga de gestionar la comunicación con un cliente específico
     * Implementa Runnable para ejecutarse en un hilo separado
     */
    class ConnectionHandler implements Runnable {

        private final Socket client;          // Socket del cliente
        private BufferedReader in;            // Flujo de entrada (mensajes recibidos)
        private PrintWriter out;              // Flujo de salida (mensajes enviados)
        private String nickname;
        private ConnectionHandler chatPartner; //Cliente al que enviarle mensajes privados

        public String getNickname() {
            return nickname;
        }

        public void setBlaj(ConnectionHandler blaj) {
            this.chatPartner = blaj;
        }

        /**
         * Constructor que recibe el socket del cliente conectado.
         */
        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        /**
         * Envía un mensaje a todos los clientes conectados.
         * @param message El mensaje que se desea enviar a todos los clientes.
         */
        public void broadcast(String message) {
            for (ConnectionHandler ch : connections) {
                if (ch != null) {
                    ch.sendMessage(message);
                }
            }
        }
        /**
         * Envía al cliente una lista de todos los usuarios conectados actualmente al servidor.
         * Muestra el nombre de usuario de cada cliente conectado.
         */
        public void userList(){
            for (ConnectionHandler ch : connections) {
                out.println("El usuario " + ch.getNickname() + " esta conectado.");
            }
        }

        /**
         * Inicia un chat privado entre el cliente actual y otro cliente especificado por su nickname.
         * @param name El nombre de usuario del cliente con el que se desea iniciar el chat privado.
         * @return true si el chat privado fue establecido exitosamente, false si no se encontró al usuario.
         */
        public boolean startPrivateChat(String name){
            for (ConnectionHandler ch : connections){
                if (ch.getNickname().equals(name)){
                    this.chatPartner = ch;
                    ch.setBlaj(this);
                    return true;
                }
            }
            return false;
        }

        /**
         * Envía un mensaje al cliente asociado a este manejador
         */
        public void sendMessage(String message) {
            out.println(message);
        }

        /**
         * Método shutdown de ConnectionHandler
         * Cierra las conexiones (entrada, salida y socket) del cliente actual.
         */
        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
                connections.remove(this);
            } catch (IOException e) {
                // Error ignorado al cerrar la conexión
            }
        }

        /**
         * Método principal que maneja la ejecución del hilo de cada cliente.
         * - Solicita al cliente un nombre de usuario.
         * - Escucha los mensajes del cliente y maneja comandos como:
         *   - /chao: Cierra la conexión.
         *   - /usuarios: Muestra la lista de usuarios conectados.
         *   - /privado <nombre>: Inicia un chat privado con otro usuario.
         * - Si el cliente está en un chat privado, redirige los mensajes solo entre los participantes.
         */
        @Override
        public void run() {
            try {
                // Inicializa los flujos de comunicación
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                // Solicita un nickname al cliente
                out.println("Por favor ingrese un nombre de usuario: ");
                nickname = in.readLine(); // Espera que el cliente ingrese un nombre
                //TODO Verificar que no se repitan usuarios
                System.out.println(nickname + " se ha conectado.");
                broadcast(nickname + " se unio al chat.");

                String message;
                // Bucle principal de recepción de mensajes
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/chao")) {
                        System.out.println(nickname + " se fue del chat.");
                        broadcast(nickname + " se fue del chat.");
                        shutdown();
                        // Mensaje normal (sin comandos)
                    } else if (message.startsWith("/usuarios")) {
                        userList();
                    } else if (message.startsWith("/privado")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2 && startPrivateChat(messageSplit[1])) {
                            sendMessage("Te has conectado a un chat privado con " + messageSplit[1]);
                            chatPartner.sendMessage("Te has conectado a un chat privado con " + nickname);
                        } else {
                            sendMessage("El usuario no existe.");
                        }
                    } else if (chatPartner != null) {
                        privateMessage(chatPartner, message);
                        //TODO añadir función para aceptar y/o salir del chat privado
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (Exception e) {
                // Si ocurre un error, se cierra la conexión del cliente
                shutdown();
            }
        }

        /**
         * Envía un mensaje privado entre el cliente actual y otro cliente especificado.
         * Este método se llama cuando el cliente está en un chat privado.
         * @param blaj El manejador de conexión del cliente con el que se está chateando.
         * @param message El mensaje que se enviará en el chat privado.
         */
        private void privateMessage(ConnectionHandler blaj, String message) {
            sendMessage(nickname + "(privado): " + message);
            blaj.sendMessage(nickname + "(privado): " + message);
        }
    }

    /**
     * Método principal del programa.
     * Crea una instancia del servidor y lo ejecuta.
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
