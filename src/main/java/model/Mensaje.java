package model;

import java.time.LocalDateTime;

public class Mensaje {
    private String id;
    private Usuario emisor;
    private Usuario receptor;
    private String contenido;
    private LocalDateTime timestamp;

    // Constructor
    public Mensaje(String id, Usuario emisor, Usuario receptor, String contenido) {
        this.id = id;
        this.emisor = emisor;
        this.receptor = receptor;
        this.contenido = contenido;
        this.timestamp = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public Usuario getEmisor() {
        return emisor;
    }

    public Usuario getReceptor() {
        return receptor;
    }

    public String getContenido() {
        return contenido;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
