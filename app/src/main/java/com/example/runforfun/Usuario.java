package com.example.runforfun;

import java.util.Date;

public class Usuario {

    String amigos = "n",
            nombre = "usarioNuevo",
            solicitudesEnviadas = "n",
            solicitudesRecibidas = "n",
            ultimaFecha = String.valueOf(new Date());
    int calorias = 0,
            caloriasDia = 0,
            pasos = 0,
            pasosDia = 0;

    public Usuario() {

    }

    public Usuario(String amigos, String nombre, String solicitudesEnviadas, String solicitudesRecibidas, int calorias, int caloriasDia, int pasos, int pasosDia, String ultimaFecha) {
        this.amigos = amigos;
        this.nombre = nombre;
        this.solicitudesEnviadas = solicitudesEnviadas;
        this.solicitudesRecibidas = solicitudesRecibidas;
        this.calorias = calorias;
        this.caloriasDia = caloriasDia;
        this.pasos = pasos;
        this.pasosDia = pasosDia;
        this.ultimaFecha = ultimaFecha;
    }

    public String getAmigos() {
        return amigos;
    }

    public void setAmigos(String amigos) {
        this.amigos = amigos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSolicitudesEnviadas() {
        return solicitudesEnviadas;
    }

    public void setSolicitudesEnviadas(String solicitudesEnviadas) {
        this.solicitudesEnviadas = solicitudesEnviadas;
    }

    public String getSolicitudesRecibidas() {
        return solicitudesRecibidas;
    }

    public void setSolicitudesRecibidas(String solicitudesRecibidas) {
        this.solicitudesRecibidas = solicitudesRecibidas;
    }

    public int getCalorias() {
        return calorias;
    }

    public void setCalorias(int calorias) {
        this.calorias = calorias;
    }

    public int getCaloriasDia() {
        return caloriasDia;
    }

    public void setCaloriasDia(int caloriasDia) {
        this.caloriasDia = caloriasDia;
    }

    public int getPasos() {
        return pasos;
    }

    public void setPasos(int pasos) {
        this.pasos = pasos;
    }

    public int getPasosDia() {
        return pasosDia;
    }

    public void setPasosDia(int pasosDia) {
        this.pasosDia = pasosDia;
    }

    public String getUltimaFecha() {
        return ultimaFecha;
    }

    public void setUltimaFecha(String ultimaFecha) {
        this.ultimaFecha = ultimaFecha;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "amigos='" + amigos + '\'' +
                ", nombre='" + nombre + '\'' +
                ", solicitudesEnviadas='" + solicitudesEnviadas + '\'' +
                ", solicitudesRecibidas='" + solicitudesRecibidas + '\'' +
                ", calorias=" + calorias +
                ", caloriasDia=" + caloriasDia +
                ", pasos=" + pasos +
                ", pasosDia=" + pasosDia +
                ", ultimaFecha=" + ultimaFecha +
                '}';
    }
}
