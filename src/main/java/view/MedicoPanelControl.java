package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.application.Application.launch;

public class MedicoPanelControl extends Application {

    private Usuario usuario;  // Añadimos un campo para el Usuario (Paciente o Médico)
    private ListView<String> listaPacientes;
    private ListView<String> listaDocumentos;
    private Map<Paciente, Long> pacientesEnEspera;
    private DocumentoManager documentoManager;

    private TextArea areaChat;
    private TextField inputMensaje;
    private Button enviarMensaje;
    private Button removerPacienteButton;
    private Button asignarConsultaButton;
    private Label notificacionesLabel;
    private ProgressIndicator progressIndicator;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private Thread listenerThread; // Hilo para escuchar mensajes entrantes
    private String serverHost = "127.0.0.1"; // Dirección IP del servidor (ajustar según la red)
    private int serverPort = 12345; // Puerto del servidor (ajustar según configuración)
    private TextField buscarPacienteField;
    private TextField buscarDocumentoField;

    private VBox root;

    // Constructor sin parámetros, necesario para que JavaFX funcione
    public MedicoPanelControl() {
    }

    // Constructor para recibir un usuario (Paciente o Médico)
    public MedicoPanelControl(Usuario usuario) {
        this.usuario = usuario;  // Almacenar el usuario que inició sesión
    }

    @Override
    public void start(Stage primaryStage) {
        pacientesEnEspera = new HashMap<>();
        documentoManager = new DocumentoManager();

        cargarDatos();  // Cargar los datos

        listaPacientes = new ListView<>();
        actualizarListaPacientes();

        listaDocumentos = new ListView<>();
        actualizarListaDocumentos();

        areaChat = new TextArea();
        areaChat.setEditable(false);

        inputMensaje = new TextField();
        inputMensaje.setPromptText("Escribe un mensaje...");

        enviarMensaje = new Button("Enviar");
        enviarMensaje.setOnAction(e -> enviarMensaje());

        removerPacienteButton = new Button("Remover Paciente");
        removerPacienteButton.setDisable(true);
        removerPacienteButton.setOnAction(e -> removerPaciente());

        asignarConsultaButton = new Button("Asignar Consulta");
        asignarConsultaButton.setDisable(true);
        asignarConsultaButton.setOnAction(e -> asignarConsulta());

        Button seleccionarArchivoButton = new Button("Seleccionar Archivo");
        seleccionarArchivoButton.setOnAction(e -> abrirBuscadorDeArchivos());

        notificacionesLabel = new Label("Notificaciones: Ninguna");

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(20));

        Button refreshButton = new Button("Refrescar Lista de Pacientes");
        refreshButton.setOnAction(e -> actualizarListaPacientes());
        Button refreshDocsButton = new Button("Refrescar Lista de Documentos");
        refreshDocsButton.setOnAction(e -> actualizarListaDocumentos());

        HBox chatBox = new HBox(10, inputMensaje, enviarMensaje);
        HBox accionesPacientesBox = new HBox(10, removerPacienteButton, asignarConsultaButton);
        HBox docBox = new HBox(10, seleccionarArchivoButton);
        HBox progressBox = new HBox(progressIndicator);

        root.getChildren().addAll(new Label("Pacientes en espera:"), listaPacientes,
                accionesPacientesBox, refreshButton, new Label("Documentos Médicos:"), listaDocumentos,
                refreshDocsButton, docBox, new Label("Chat"), areaChat, chatBox, new Label("Notificaciones"),
                notificacionesLabel, progressBox);

        listaPacientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selectedPacienteDesc = listaPacientes.getSelectionModel().getSelectedItem();
                Paciente paciente = obtenerPacientePorDescripcion(selectedPacienteDesc);
                mostrarDetallesPaciente(paciente);
                removerPacienteButton.setDisable(false);
                asignarConsultaButton.setDisable(false);
            }
        });

        Scene scene = new Scene(root, 600, 800);
        primaryStage.setTitle("Panel de Control del Usuario");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    // Método para configurar búsqueda de pacientes
    private void configurarBusquedaPacientes() {
        buscarPacienteField = new TextField();
        buscarPacienteField.setPromptText("Buscar paciente...");
        buscarPacienteField.textProperty().addListener((observable, oldValue, newValue) -> filtrarPacientes(newValue));
        root.getChildren().add(0, buscarPacienteField); // Añadir al principio
    }

    // Método para filtrar pacientes
    private void filtrarPacientes(String textoBusqueda) {
        listaPacientes.getItems().clear();
        for (Map.Entry<Paciente, Long> entry : pacientesEnEspera.entrySet()) {
            Paciente paciente = entry.getKey();
            Long tiempoEspera = entry.getValue();
            if (paciente.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase())) {
                listaPacientes.getItems().add(paciente.getNombre() + " - Espera: " + tiempoEspera + " segundos");
            }
        }
    }

    // Método para configurar búsqueda de documentos
    private void configurarBusquedaDocumentos() {
        buscarDocumentoField = new TextField();
        buscarDocumentoField.setPromptText("Buscar documento...");
        buscarDocumentoField.textProperty().addListener((observable, oldValue, newValue) -> filtrarDocumentos(newValue));
        root.getChildren().add(1, buscarDocumentoField); // Añadir después de los pacientes
    }

    // Método para filtrar documentos
    private void filtrarDocumentos(String textoBusqueda) {
        listaDocumentos.getItems().clear();
        for (Documento doc : documentoManager.getDocumentos().values()) {
            if (doc.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase())) {
                String descripcion = doc.getNombre() + " - Tipo: " + doc.getTipo() +
                        " - Propietario: " + doc.getPropietario().getNombre();
                listaDocumentos.getItems().add(descripcion);
            }
        }
    }

    // Método para actualizar la lista de pacientes
    private void actualizarListaPacientes() {
        listaPacientes.getItems().clear();
        for (Map.Entry<Paciente, Long> entry : pacientesEnEspera.entrySet()) {
            Paciente paciente = entry.getKey();
            Long tiempoEspera = entry.getValue();
            listaPacientes.getItems().add(paciente.getNombre() + " - Espera: " + tiempoEspera + " segundos");
        }
    }

    // Método para obtener un paciente por su descripción
    private Paciente obtenerPacientePorDescripcion(String descripcion) {
        for (Map.Entry<Paciente, Long> entry : pacientesEnEspera.entrySet()) {
            Paciente paciente = entry.getKey();
            Long tiempoEspera = entry.getValue();
            String pacienteDesc = paciente.getNombre() + " - Espera: " + tiempoEspera + " segundos";
            if (pacienteDesc.equals(descripcion)) {
                return paciente;
            }
        }
        return null;
    }

    // Método para mostrar detalles del paciente
    private void mostrarDetallesPaciente(Paciente paciente) {
        if (paciente != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalles del Paciente");
            alert.setHeaderText("Detalles del paciente: " + paciente.getNombre());
            alert.setContentText("ID: " + paciente.getId() +
                    "\nNombre: " + paciente.getNombre() +
                    "\nCorreo: " + paciente.getEmail());
            alert.showAndWait();
        }
    }

    // Método para remover un paciente
    private void removerPaciente() {
        String selectedPacienteDesc = listaPacientes.getSelectionModel().getSelectedItem();
        Paciente paciente = obtenerPacientePorDescripcion(selectedPacienteDesc);

        if (paciente != null) {
            pacientesEnEspera.remove(paciente);
            actualizarListaPacientes();
            removerPacienteButton.setDisable(true);
            asignarConsultaButton.setDisable(true);
            areaChat.appendText("Paciente " + paciente.getNombre() + " removido de la lista de espera.\n");
            actualizarNotificaciones("Paciente " + paciente.getNombre() + " removido de la lista.");
            // Guardar datos después de realizar la acción
            guardarDatos();
        }
    }

    // Método para asignar una consulta
    private void asignarConsulta() {
        String selectedPacienteDesc = listaPacientes.getSelectionModel().getSelectedItem();
        Paciente paciente = obtenerPacientePorDescripcion(selectedPacienteDesc);

        if (paciente != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Asignar Consulta");
            dialog.setHeaderText("Asignar consulta al paciente: " + paciente.getNombre());
            dialog.setContentText("Ingresa la descripción de la consulta:");

            Optional<String> resultado = dialog.showAndWait();
            resultado.ifPresent(descripcion -> {
                areaChat.appendText("Consulta asignada a " + paciente.getNombre() + ": " + descripcion + "\n");
                actualizarNotificaciones("Consulta asignada a " + paciente.getNombre());
            });
        }
    }

    // Método para enviar un mensaje
    private void enviarMensaje() {
        String mensaje = inputMensaje.getText();
        if (!mensaje.isEmpty()) {
            areaChat.appendText("Médico: " + mensaje + "\n");
            inputMensaje.clear();
            actualizarNotificaciones("Nuevo mensaje enviado.");
        }
    }

    // Método para simular recepción de mensajes
    private void recibirMensajeSimulado(String remitente, String mensaje) {
        areaChat.appendText(remitente + ": " + mensaje + "\n");
        actualizarNotificaciones("Nuevo mensaje de " + remitente);
    }

    // Método para actualizar la lista de documentos
    private void actualizarListaDocumentos() {
        listaDocumentos.getItems().clear();
        if (documentoManager.getDocumentos().isEmpty()) {
            listaDocumentos.getItems().add("No hay documentos disponibles.");
            return;
        }
        for (Documento doc : documentoManager.getDocumentos().values()) {
            String descripcion = doc.getNombre() + " - Tipo: " + doc.getTipo() +
                    " - Propietario: " + doc.getPropietario().getNombre();
            listaDocumentos.getItems().add(descripcion);
        }
    }
    public void mostrarPanel(Stage stage) {
        // Llamamos a la función 'start' que es la encargada de mostrar la interfaz
        start(stage);
    }

    // Método para abrir el buscador de archivos
    private void abrirBuscadorDeArchivos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un archivo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(null);

        // Mostrar ProgressIndicator cuando se inicia el proceso de carga
        progressIndicator.setVisible(true);

        if (archivoSeleccionado != null) {
            String documentoId = UUID.randomUUID().toString();
            Documento documento = new Documento(
                    documentoId,
                    archivoSeleccionado.getName(),
                    archivoSeleccionado.getAbsolutePath(),
                    new Medico("2", "Dra. Ana López", "ana@mail.com", "67890", "Cardiología"),
                    "Otro"
            );

            boolean documentoSubido = documentoManager.subirDocumento(documento);
            if (documentoSubido) {
                areaChat.appendText("Documento enviado: " + archivoSeleccionado.getName() + "\n");
                actualizarListaDocumentos();
                actualizarNotificaciones("Nuevo documento enviado: " + archivoSeleccionado.getName());
            } else {
                areaChat.appendText("Error al enviar el documento.\n");
            }

            // Ocultar ProgressIndicator después de subir el documento
            progressIndicator.setVisible(false);

            // Guardar los datos después de la acción
            guardarDatos();
        } else {
            areaChat.appendText("No se seleccionó ningún archivo.\n");
            progressIndicator.setVisible(false);  // Ocultar si no se seleccionó archivo
        }
    }

    // Método para guardar los datos en archivos JSON
    public void guardarDatos() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("pacientes.json"), new ArrayList<>(pacientesEnEspera.keySet()));
            mapper.writeValue(new File("documentos.json"), new ArrayList<>(documentoManager.getDocumentos().values()));
            System.out.println("Datos guardados correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para cargar los datos desde los archivos JSON
    public void cargarDatos() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Verificar si el archivo de pacientes existe, si no, crear uno con datos predeterminados
            File pacientesFile = new File("pacientes.json");
            if (!pacientesFile.exists()) {
                System.out.println("Archivo de pacientes no encontrado, creando archivo...");
                List<Paciente> pacientes = new ArrayList<>();
                // Crear algunos pacientes predeterminados
                pacientes.add(new Paciente("1", "Juan Pérez", "juan@mail.com", "12345"));
                pacientes.add(new Paciente("2", "María López", "maria@mail.com", "67890"));
                mapper.writeValue(pacientesFile, pacientes);
                pacientesEnEspera.putAll(pacientes.stream().collect(Collectors.toMap(p -> p, p -> 10L))); // Asignar tiempo de espera predeterminado
            } else {
                // Cargar los pacientes desde el archivo
                List<Paciente> pacientes = mapper.readValue(pacientesFile,
                        mapper.getTypeFactory().constructCollectionType(List.class, Paciente.class));
                for (Paciente paciente : pacientes) {
                    pacientesEnEspera.put(paciente, 10L);
                }
            }

            // Verificar si el archivo de documentos existe, si no, crear uno con datos predeterminados
            File documentosFile = new File("documentos.json");
            if (!documentosFile.exists()) {
                System.out.println("Archivo de documentos no encontrado, creando archivo...");
                List<Documento> documentos = new ArrayList<>();
                // Crear algunos documentos predeterminados
                documentos.add(new Documento("1", "Informe de consulta", "/path/to/file", new Medico("2", "Dra. Ana López", "ana@mail.com", "67890", "Cardiología"), "PDF"));
                mapper.writeValue(documentosFile, documentos);
                documentoManager.subirDocumento(documentos.get(0)); // Añadir el documento a la lista de documentos
            } else {
                // Cargar los documentos desde el archivo
                List<Documento> documentos = mapper.readValue(documentosFile,
                        mapper.getTypeFactory().constructCollectionType(List.class, Documento.class));
                for (Documento documento : documentos) {
                    documentoManager.subirDocumento(documento);
                }
            }

            System.out.println("Datos cargados correctamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar las notificaciones
    private void actualizarNotificaciones(String mensaje) {
        Platform.runLater(() -> notificacionesLabel.setText("Notificaciones: " + mensaje));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
