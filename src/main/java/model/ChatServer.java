package model;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private DocumentoManager documentoManager;
    private UsuarioManager usuarioManager;
    private HashMap<String, Usuario> usuariosConectados; // Usuarios actualmente conectados

    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newCachedThreadPool();
        documentoManager = new DocumentoManager();
        usuarioManager = new UsuarioManager(); // Inicializamos el manejador de usuarios
        usuariosConectados = new HashMap<>();
    }

    public void start() {
        System.out.println("Servidor de chat y gestión de documentos iniciado...");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                threadPool.execute(new ClientHandler(clientSocket));  // Crear un hilo para cada cliente
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private Usuario usuario;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Enviar mensaje de bienvenida con opciones
                out.println("Bienvenido al chat médico! Puede enviarnos uno de los siguientes comandos:");

                String input;
                while ((input = in.readLine()) != null) {
                    handleComando(input); // Redirige a la función de manejo de comandos
                    if (usuario != null) break;  // Si el login o registro es exitoso, romper el ciclo
                }

                // Continuar con la interacción del chat solo si el usuario está autenticado
                if (usuario != null) {
                    while ((input = in.readLine()) != null) {
                        // Comandos para interactuar con el servidor una vez logueado
                        if (input.startsWith("enviar_documento")) {
                            handleEnviarDocumento(input);
                        } else if (input.startsWith("ver_documentos")) {
                            handleVerDocumentos();
                        } else if (input.startsWith("enviar_mensaje")) {
                            handleEnviarMensaje(input);
                        } else {
                            out.println("Comando no reconocido.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (usuario != null) {
                        usuariosConectados.remove(usuario.getEmail());
                        System.out.println("Usuario desconectado: " + usuario.getEmail());
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleComando(String input) {
            // Verificamos que el comando no esté vacío o nulo
            if (input == null || input.trim().isEmpty()) {
                out.println("Comando vacío. Por favor ingrese un comando válido.");
                return;
            }

            // Dividimos el comando por espacios
            String[] comando = input.split("\\s+");

            // Si el comando es 'login'
            if (comando[0].equalsIgnoreCase("login")) {
                handleLogin(comando);
            }
            // Si el comando es 'registrar'
            else if (comando[0].equalsIgnoreCase("registrar")) {
                handleRegistrar(comando);
            } else {
                out.println("Comando no reconocido. Usa 'login' o 'registrar'.");
            }
        }

        private void handleLogin(String[] comando) {
            if (comando.length < 3) {
                out.println("Formato incorrecto para login. Use: 'login [email] [password]'.");
                return;
            }

            String email = comando[1];
            String password = comando[2];

            Usuario usuarioAutenticado = usuarioManager.autenticar(email, password);
            if (usuarioAutenticado != null) {
                this.usuario = usuarioAutenticado;
                usuariosConectados.put(usuario.getEmail(), usuario); // Guardamos al usuario en la lista de conectados
                out.println("Login exitoso. Bienvenido, " + usuarioAutenticado.getNombre());
            } else {
                out.println("Credenciales incorrectas. Verifique su email y contraseña.");
            }
        }

        private void handleRegistrar(String[] comando) {
            if (comando.length < 4) {
                out.println("Formato incorrecto para registro. Use: 'registrar [email] [nombre] [password]'.");
                return;
            }

            String email = comando[1];
            String nombre = comando[2];
            String password = comando[3];

            boolean registroExitoso = usuarioManager.registrar(email, nombre, password);
            if (registroExitoso) {
                out.println("Registro exitoso. Ahora puede iniciar sesión.");
            } else {
                out.println("Error en el registro. Verifique el email o intente nuevamente.");
            }
        }

        private void handleEnviarDocumento(String input) {
            String[] parts = input.split(" ");
            if (parts.length < 5) {
                out.println("Error: El comando 'enviar_documento' debe tener el formato: 'enviar_documento [id] [nombre] [ruta] [tipo]'");
                return;
            }

            String id = parts[1];
            String nombre = parts[2];
            String ruta = parts[3];
            String tipo = parts[4];

            Documento documento = new Documento(id, nombre, ruta, usuario, tipo);
            boolean documentoSubido = documentoManager.subirDocumento(documento);

            if (documentoSubido) {
                out.println("Documento '" + nombre + "' recibido y almacenado.");
                if (usuario instanceof Paciente) {
                    ((Paciente) usuario).agregarDocumento("Fecha actual", documento);
                }
            } else {
                out.println("Error al subir el documento.");
            }
        }

        private void handleVerDocumentos() {
            StringBuilder documentosLista = new StringBuilder("Documentos en el sistema:\n");
            if (documentoManager.getDocumentos().isEmpty()) {
                out.println("No hay documentos almacenados.");
            } else {
                for (Documento doc : documentoManager.getDocumentos().values()) {
                    documentosLista.append("ID: ").append(doc.getId())
                            .append(", Nombre: ").append(doc.getNombre())
                            .append(", Propietario: ").append(doc.getPropietario().getNombre())
                            .append(", Tipo: ").append(doc.getTipo()).append("\n");
                }
                out.println(documentosLista.toString());
            }
        }

        private void handleEnviarMensaje(String input) {
            String[] parts = input.split(" ", 2);
            if (parts.length < 2) {
                out.println("Error: El comando 'enviar_mensaje' debe tener el formato: 'enviar_mensaje [mensaje]'");
                return;
            }

            String mensaje = parts[1];
            enviarAMedicosYPacientes(mensaje);
        }

        private void enviarAMedicosYPacientes(String mensaje) {
            for (Usuario u : usuariosConectados.values()) {
                if (u != usuario) {
                    try {
                        PrintWriter outCliente = new PrintWriter(u.getSocket().getOutputStream(), true);
                        outCliente.println(usuario.getNombre() + ": " + mensaje);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer(12345); // Puerto de escucha
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
