package model;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_HOST = "127.0.0.1"; // IP del servidor
    private static final int SERVER_PORT = 12345; // Puerto del servidor

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Constructor que ahora usa las constantes SERVER_HOST y SERVER_PORT
    public ChatClient() throws IOException {
        // Establecer la conexión con el servidor usando la IP y puerto definidos
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Método principal para manejar la conexión y la interacción
    public void start() throws IOException {
        try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {
            // Leer mensaje de bienvenida del servidor
            String serverMessage = in.readLine();
            System.out.println("Servidor: " + serverMessage);

            // Pedir al usuario que ingrese su acción
            System.out.print("¿Desea 'login' o 'registrar'? ");
            String action = console.readLine().toLowerCase();

            if (action.equals("registrar")) {
                handleRegistro(console);
            } else if (action.equals("login")) {
                handleLogin(console);
            } else {
                System.out.println("Acción no reconocida. Debe elegir entre 'login' o 'registrar'.");
            }

            // Continuar con la comunicación una vez logueado o registrado
            String serverResponse = in.readLine();
            System.out.println("Servidor: " + serverResponse);

            if (serverResponse.equals("Login exitoso.") || serverResponse.equals("Registro exitoso. Ahora puede iniciar sesión con su cuenta.")) {
                // Continuar con el chat después de login o registro exitoso
                String userInput;
                while ((userInput = console.readLine()) != null) {
                    out.println(userInput); // Enviar cualquier comando o mensaje
                    serverResponse = in.readLine();
                    System.out.println("Servidor: " + serverResponse);
                }
            }
        } finally {
            socket.close();
        }
    }

    // Manejo del login
    private void handleLogin(BufferedReader console) throws IOException {
        System.out.print("Email: ");
        String email = console.readLine();
        System.out.print("Contraseña: ");
        String password = console.readLine();

        out.println("login " + email + " " + password); // Enviar datos de login
    }

    // Manejo del registro
    private void handleRegistro(BufferedReader console) throws IOException {
        System.out.print("Email: ");
        String email = console.readLine();
        System.out.print("Nombre: ");
        String nombre = console.readLine();
        System.out.print("Contraseña: ");
        String password = console.readLine();

        out.println("registrar " + email + " " + nombre + " " + password); // Enviar datos de registro
    }

    // Método principal para arrancar el cliente
    public static void main(String[] args) {
        try {
            ChatClient client = new ChatClient(); // El cliente ahora se conecta con la IP del servidor
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
