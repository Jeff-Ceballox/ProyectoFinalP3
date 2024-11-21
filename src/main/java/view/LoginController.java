package view;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Medico;
import model.Paciente;
import model.Usuario;
import model.UsuarioManager;

import java.util.Optional;

public class LoginController {

    private UsuarioManager usuarioManager;
    private Stage stage;

    public LoginController(Stage stage, UsuarioManager usuarioManager) {
        this.stage = stage;
        this.usuarioManager = usuarioManager; // Manejador de usuarios cargados desde archivo o red
    }

    // Mostrar la interfaz de login
    public void mostrarLogin() {
        VBox root = new VBox(10);
        root.setPadding(new javafx.geometry.Insets(20));

        // Campos para el email y la contraseña
        TextField emailField = new TextField();
        emailField.setPromptText("Correo Electrónico");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");

        // Botón de login
        Button loginButton = new Button("Iniciar Sesión");
        loginButton.setOnAction(e -> validarLogin(emailField.getText(), passwordField.getText()));

        root.getChildren().addAll(new Label("Iniciar Sesión"), emailField, passwordField, loginButton);

        Scene scene = new Scene(root, 300, 200);
        stage.setTitle("Login - Sistema Médico");
        stage.setScene(scene);
        stage.show();
    }

    // Validar las credenciales de login
    private void validarLogin(String email, String password) {
        Usuario usuario = usuarioManager.autenticar(email, password);
        if (usuario != null) {
            // Usuario autenticado
        } else {
            // Credenciales inválidas
        }

    }

    // Mostrar mensaje de error
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Login");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Mostrar el panel del médico
    private void mostrarPanelMedico(Medico medico) {
        MedicoPanelControl medicoPanel = new MedicoPanelControl(medico);
        medicoPanel.mostrarPanel(stage);
    }

    // Mostrar el panel del paciente (debes implementar esta clase)
    private void mostrarPanelPaciente(Paciente paciente) {
        MedicoPanelControl pacientePanel = new MedicoPanelControl(paciente);
        pacientePanel.mostrarPanel(stage);
    }
}
