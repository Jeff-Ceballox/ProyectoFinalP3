package model;

import java.util.HashMap;

public class DocumentoManager {
    private HashMap<String, Documento> documentos; // Key: id del documento

    public DocumentoManager() {
        this.documentos = new HashMap<>();
    }

    // Subir un documento (agregarlo al sistema)
    public boolean subirDocumento(Documento documento) {
        if (documentos.containsKey(documento.getId())) {
            System.out.println("Error: El documento ya existe.");
            return false;
        }
        documentos.put(documento.getId(), documento);
        System.out.println("Documento agregado con éxito: " + documento.getNombre()); // Verificación
        return true;
    }


    // Obtener un documento por su id
    public Documento obtenerDocumento(String id) {
        return documentos.get(id);
    }

    // Obtener todos los documentos
    public HashMap<String, Documento> getDocumentos() {
        return documentos;
    }

    // Mostrar todos los documentos
    public void mostrarDocumentos() {
        System.out.println("Mostrando todos los documentos...");
        for (Documento doc : documentos.values()) {
            System.out.println(doc);
        }
    }
}
