package model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Documento {
    private String id;
    private String nombre;
    private String ruta;
    private Usuario propietario;
    private String tipo;

    // Constructor sin parámetros (necesario para Jackson)
    public Documento() {
        this.id = "";
        this.nombre = "";
        this.ruta = "";
        this.propietario = null;
        this.tipo = "";
    }

    // Constructor con parámetros
    @JsonCreator
    public Documento(@JsonProperty("id") String id,
                     @JsonProperty("nombre") String nombre,
                     @JsonProperty("ruta") String ruta,
                     @JsonProperty("propietario") Usuario propietario,
                     @JsonProperty("tipo") String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.ruta = ruta;
        this.propietario = propietario;
        this.tipo = tipo;
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

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public Usuario getPropietario() {
        return propietario;
    }

    public void setPropietario(Usuario propietario) {
        this.propietario = propietario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
