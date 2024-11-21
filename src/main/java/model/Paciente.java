package model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

public class Paciente extends Usuario {
    private HashMap<String, String> historialMedico; // Key: Fecha, Value: Descripción o documento

    // Constructor sin parámetros (necesario para Jackson)
    public Paciente() {
        super();  // Llamamos al constructor sin parámetros de la clase base (Usuario)
        this.historialMedico = new HashMap<>();
    }

    // Constructor con parámetros
    @JsonCreator
    public Paciente(@JsonProperty("id") String id,
                    @JsonProperty("nombre") String nombre,
                    @JsonProperty("email") String email,
                    @JsonProperty("password") String password) {
        super(id, nombre, email, password);  // Llamamos al constructor de la clase base (Usuario) con los parámetros adecuados
        this.historialMedico = new HashMap<>();
    }

    public HashMap<String, String> getHistorialMedico() {
        return historialMedico;
    }

    // Método para agregar un documento al historial médico
    public void agregarDocumento(String fecha, Documento documento) {
        historialMedico.put(fecha, "Documento: " + documento.getNombre());
    }
}
