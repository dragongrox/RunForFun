package com.example.runforfun;

import java.util.Map;

public class Mensaje {
    String texto;
    String autor;

    public Mensaje() {
        texto = "";
        autor = "";
    }

    public Mensaje(String texto, String autor) {
        this.texto = texto;
        this.autor = autor;
    }

    public Mensaje(Map<String, Object> map) {
        this.texto = (String) (map.get("texto"));
        this.autor = (String) (map.get("texto"));
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    @Override
    public String toString() {
        return "Mensaje{" +
                "texto='" + texto + '\'' +
                ", autor='" + autor + '\'' +
                '}';
    }
}
