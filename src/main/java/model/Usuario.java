package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.Socket;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Usuario {
    private String id;
    private String nombre;
    private String email;
    private String password;
    private Socket socket;

    // Constructor con parámetros (para inicialización normal)
    public Usuario(String id, String nombre, String email, String password, Socket socket) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.socket = socket;
    }
    public Usuario(String email, String nombre, String password) {
        this.email = email;
        this.nombre = nombre;
        this.password = password;
    }
    // Constructor sin parámetros (para crear instancias sin necesidad de Socket)
    public Usuario(String id, String nombre, String email, String password) {
        this(id, nombre, email, password, null);  // Llamamos al constructor anterior, pasando null para el Socket
    }

    // Constructor sin parámetros (necesario para Jackson)
    public Usuario() {
        this.id = "";
        this.nombre = "";
        this.email = "";
        this.password = "";
        this.socket = null;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
