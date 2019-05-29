package com.example.runforfun;

import java.util.Map;

public class Posicion {
    double lat, lon;
    String fecha;

    public Posicion(double lat, double lon, String fecha) {
        this.lat = lat;
        this.lon = lon;
        this.fecha = fecha;
    }

    public Posicion(Map<String, String> map) {
        Object latS = map.get("lat");
        Object lonS = map.get("lon");
        this.lat = (double) (latS);
        this.lon = (double) (lonS);
        this.fecha = map.get("fecha");
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Posicion{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", fecha='" + fecha + '\'' +
                '}';
    }
}
