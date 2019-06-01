package com.example.runforfun;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //Almacenamos en un array las posiciones de nustro usuario y las almacenamos en un polylineOptions punto por punto para ir dibujando la ruta
        ArrayList posiciones = MainActivity.usuario.posiciones;
        PolylineOptions polylineOptions = new PolylineOptions();
        LatLng latLngUltimaPosicion = null;
        for (int cont = 0; cont < posiciones.size(); cont++) {
            Map<String, String> map = (HashMap) posiciones.get(cont);
            Posicion posicion = new Posicion(map);
            polylineOptions.add(new LatLng(posicion.lat, posicion.lon));
            //tambien almacenamos la ultima posicion para poder centrar la camara posteriormente alli
            latLngUltimaPosicion = new LatLng(posicion.lat, posicion.lon);
        }
        //le pasamos el trazado al mapa para que lo dibuje al usuario
        mMap.addPolyline(polylineOptions);
        //por ultimo creamos un CameraPosition que definira la posicion de la camara al iniciar el mapa
        //de esta forma centraremos la vista en el ultimo punto de nuestra ruta
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLngUltimaPosicion).zoom(20f).tilt(30f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);

    }
}
