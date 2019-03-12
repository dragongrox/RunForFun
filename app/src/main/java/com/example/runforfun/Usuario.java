package com.example.runforfun;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * clase que se encarga de representar y contener todos los datos de un usuario
 */
public class Usuario {

    String amigos = "n",
            nombre = "usarioNuevo",
            solicitudesEnviadas = "n",
            solicitudesRecibidas = "n",
            ultimaFecha;
    int pasos = 0,
            pasosDia = 0,
            altura = 160,
            peso = 70;
    double distanciaDia = 0,
            distancia = 0;

    float calorias = 0,
            caloriasDia = 0;

    public Usuario() {
        //obtenemos la fecha actual
        Calendar calendario = GregorianCalendar.getInstance();
        Date fecha = calendario.getTime();
        System.out.println(fecha);
        SimpleDateFormat formatoDeFecha = new SimpleDateFormat("dd/MM/yyyy");
        ultimaFecha = (formatoDeFecha.format(fecha));

    }

    public Usuario(String amigos, String nombre, String solicitudesEnviadas, String solicitudesRecibidas, String ultimaFecha, float calorias, float caloriasDia, int pasos, int pasosDia, int altura, int peso, double distanciaDia, double distancia) {
        this.amigos = amigos;
        this.nombre = nombre;
        this.solicitudesEnviadas = solicitudesEnviadas;
        this.solicitudesRecibidas = solicitudesRecibidas;
        this.ultimaFecha = ultimaFecha;
        this.calorias = calorias;
        this.caloriasDia = caloriasDia;
        this.pasos = pasos;
        this.pasosDia = pasosDia;
        this.altura = altura;
        this.peso = peso;
        this.distanciaDia = distanciaDia;
        this.distancia = distancia;
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

    public float getCalorias() {
        return calorias;
    }

    public void setCalorias(float calorias) {
        this.calorias = calorias;
    }

    public float getCaloriasDia() {
        return caloriasDia;
    }

    public void setCaloriasDia(float caloriasDia) {
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

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public double getDistanciaDia() {
        return distanciaDia;
    }

    public void setDistanciaDia(double distanciaDia) {
        this.distanciaDia = distanciaDia;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "amigos='" + amigos + '\'' +
                ", nombre='" + nombre + '\'' +
                ", solicitudesEnviadas='" + solicitudesEnviadas + '\'' +
                ", solicitudesRecibidas='" + solicitudesRecibidas + '\'' +
                ", ultimaFecha='" + ultimaFecha + '\'' +
                ", calorias=" + calorias +
                ", caloriasDia=" + caloriasDia +
                ", pasos=" + pasos +
                ", pasosDia=" + pasosDia +
                ", altura=" + altura +
                ", peso=" + peso +
                ", distanciaDia=" + distanciaDia +
                ", distancia=" + distancia +
                '}';
    }


}
