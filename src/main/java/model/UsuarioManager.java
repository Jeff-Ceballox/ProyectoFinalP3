package model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class UsuarioManager {
    private HashMap<String, Usuario> usuarios; // Key: email, Value: Usuario

    public UsuarioManager() {
        usuarios = new HashMap<>();
        cargarUsuarios();
        if (usuarios.isEmpty()) {
            cargarUsuariosEjemplo(); // Crear usuarios de ejemplo si no hay datos cargados
        }
    }

    // Cargar usuarios desde archivo JSON
    private void cargarUsuarios() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("usuarios.json");

        if (file.exists()) {
            try {
                Usuario[] usuariosArray = mapper.readValue(file, Usuario[].class);
                for (Usuario usuario : usuariosArray) {
                    usuarios.put(usuario.getEmail(), usuario);
                }
                System.out.println("Usuarios cargados correctamente desde usuarios.json.");
            } catch (IOException e) {
                System.err.println("Error al cargar usuarios desde el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("El archivo usuarios.json no existe. Se inicializará un sistema vacío.");
        }
    }

    // Guardar usuarios en archivo JSON
    public void guardarUsuarios() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Guardar usuarios en un archivo JSON
            mapper.writeValue(new File("usuarios.json"), usuarios.values());
            System.out.println("Usuarios guardados correctamente en usuarios.json.");
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios en el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Crear usuarios de ejemplo si no hay datos cargados
    private void cargarUsuariosEjemplo() {
        // Si no hay usuarios cargados desde el archivo, añadir usuarios de ejemplo
        Usuario usuario1 = new Usuario("1", "Juan", "juan@mail.com", "12345");
        Usuario usuario2 = new Usuario("2", "Maria", "maria@mail.com", "54321");
        usuarios.put(usuario1.getEmail(), usuario1);
        usuarios.put(usuario2.getEmail(), usuario2);
        guardarUsuarios(); // Guardamos los usuarios de ejemplo en el archivo JSON
        System.out.println("Usuarios de ejemplo creados.");
    }
    public boolean existeEmail(String email) {
        return usuarios.containsKey(email);  // Verifica si el email ya está en el mapa
    }

    // Registrar un nuevo usuario
    public boolean registrarUsuario(Usuario usuario) {
        if (usuarios.containsKey(usuario.getEmail())) {
            return false; // El usuario ya está registrado
        }
        usuarios.put(usuario.getEmail(), usuario);
        guardarUsuarios(); // Guardamos el archivo para persistir los cambios
        return true; // Registro exitoso
    }
    public boolean registrar(String email, String nombre, String password) {
        if (usuarios.containsKey(email)) {
            return false;  // Ya existe un usuario con este email
        }

        // Crear el nuevo usuario y agregarlo a la lista
        Usuario nuevoUsuario = new Usuario(email, nombre, password);
        usuarios.put(email, nuevoUsuario);
        return true;  // Registro exitoso
    }
    // Autenticar un usuario
    public Usuario autenticar(String email, String password) {
        Usuario usuario = usuarios.get(email);
        if (usuario != null && usuario.getPassword().equals(password)) {
            return usuario; // Credenciales correctas
        }
        return null; // Credenciales incorrectas
    }

    // Obtener todos los usuarios
    public Collection<Usuario> getUsuarios() {
        return usuarios.values();
    }

    // Buscar usuario por email
    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarios.get(email);
    }

    // Eliminar un usuario
    public boolean eliminarUsuario(String email) {
        if (usuarios.containsKey(email)) {
            usuarios.remove(email);
            guardarUsuarios();
            System.out.println("Usuario con email " + email + " eliminado correctamente.");
            return true;
        }
        System.out.println("Error: No se encontró un usuario con email " + email + " para eliminar.");
        return false;
    }
}
