package main;

import model.*;

public class MainApp {
    public static void main(String[] args) {
        // Crear instancias de usuarios
        Paciente paciente = new Paciente("1", "Juan Pérez", "juan@mail.com", "12345");
        Medico medico = new Medico("2", "Dra. Ana López", "ana@mail.com", "67890", "Cardiología");

        // Crear el DocumentoManager
        DocumentoManager documentoManager = new DocumentoManager();

        // Crear un documento para el paciente
        Documento documentoPaciente = new Documento("101", "Radiografía de tórax", "C:\\Users\\ASUS\\Documents\\Clase Principal", paciente, "Radiografia");
        documentoManager.subirDocumento(documentoPaciente);

        // Agregar el documento al historial del paciente
        paciente.agregarDocumento("2024-11-19", documentoPaciente);

        // Crear un documento para el médico
        Documento documentoMedico = new Documento("102", "Receta Médica", "C:\\Users\\ASUS\\Downloads\\Proyecto-final (2)", medico, "Receta Medica");
        documentoManager.subirDocumento(documentoMedico);

        // Enviar el documento del médico al paciente
        medico.enviarDocumento(paciente, documentoMedico);

        // Mostrar los documentos
        System.out.println("Historial médico de " + paciente.getNombre() + ": " + paciente.getHistorialMedico());
        documentoManager.mostrarDocumentos(); // Mostrar todos los documentos en el sistema
    }
}
