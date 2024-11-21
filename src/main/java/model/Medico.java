package model;

import java.util.HashMap;

public class Medico extends Usuario {
    private String especialidad;
    private HashMap<Paciente, Long> pacientesEnEspera; // Key: Paciente, Value: Tiempo de espera

    // Constructor
    public Medico(String id, String nombre, String email, String password, String especialidad) {
        super(id, nombre, email, password);
        this.especialidad = especialidad;
        this.pacientesEnEspera = new HashMap<>();
    }

    // Métodos
    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public HashMap<Paciente, Long> getPacientesEnEspera() {
        return pacientesEnEspera;
    }

    public void agregarPacienteEnEspera(Paciente paciente, long tiempoEspera) {
        pacientesEnEspera.put(paciente, tiempoEspera);
    }

    // Método para enviar un documento a un paciente
    public void enviarDocumento(Paciente paciente, Documento documento) {
        paciente.agregarDocumento("Fecha actual", documento); // Agregar al historial del paciente
        System.out.println("Documento enviado a " + paciente.getNombre() + ": " + documento.getNombre());
    }
}
