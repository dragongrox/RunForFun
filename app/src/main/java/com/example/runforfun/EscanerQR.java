package com.example.runforfun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class EscanerQR extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ImageView imageViewQRPropio;
    Bitmap bitmap;
    private ZXingScannerView zXingScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escaner_qr);
        imageViewQRPropio = findViewById(R.id.imageViewQRPropio);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EscanerQR.this,
                    new String[]{Manifest.permission.CAMERA}, 1000);
        }

        generarQRPropio();
    }


    public void Escanear(View view) {
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        zXingScannerView.setFormats(ZXingScannerView.ALL_FORMATS);
        setContentView(zXingScannerView);
        //"Activa" el escaner de la aplicacion
        zXingScannerView.setResultHandler(this);
        //Icicia
        zXingScannerView.startCamera();
    }

    public static String palabraEliminar(String frase, String palabra) {
        if (frase.contains(palabra))
            return frase.replaceAll(palabra, "");
        return frase;
    }

    @Override
    public void handleResult(Result result) {
        //Manejamos el resultado
        //Escriibimos el resultado
        String nombreAmigo = result.getText();
        //inicializamos el usuario amigo
        Usuario usuarioAmigo = new Usuario();
        //obtenemos las solicitudes recibidas del usuario amigo para añadir una nuestra
        MainActivity.databaseReferenceAmigo = MainActivity.database.getReference(nombreAmigo);
        MainActivity.databaseReferenceAmigo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuarioAmigo.setAmigos(dataSnapshot.child("amigos").getValue().toString());
                usuarioAmigo.setCalorias(Float.parseFloat(dataSnapshot.child("calorias").getValue().toString()));
                usuarioAmigo.setCaloriasDia(Float.parseFloat(dataSnapshot.child("caloriasDia").getValue().toString()));
                usuarioAmigo.setNombre(dataSnapshot.child("nombre").getValue().toString());
                usuarioAmigo.setPasos(Integer.parseInt(dataSnapshot.child("pasos").getValue().toString()));
                usuarioAmigo.setPasosDia(Integer.parseInt(dataSnapshot.child("pasosDia").getValue().toString()));
                usuarioAmigo.setSolicitudesEnviadas(dataSnapshot.child("solicitudesEnviadas").getValue().toString());
                usuarioAmigo.setSolicitudesRecibidas(dataSnapshot.child("solicitudesRecibidas").getValue().toString());
                usuarioAmigo.setUltimaFecha(dataSnapshot.child("ultimaFecha").getValue().toString());
                usuarioAmigo.setAltura(Integer.parseInt(dataSnapshot.child("altura").getValue().toString()));
                usuarioAmigo.setPeso(Integer.parseInt(dataSnapshot.child("peso").getValue().toString()));
                usuarioAmigo.setDistancia(Double.parseDouble(dataSnapshot.child("distancia").getValue().toString()));
                usuarioAmigo.setDistanciaDia(Double.parseDouble(dataSnapshot.child("distanciaDia").getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //comprobamos que el amigo no este ya añadido
        if (!MainActivity.usuario.getAmigos().contains(nombreAmigo)) {
            //comprobamos el estado de las solicitudes
            if (MainActivity.usuario.getSolicitudesRecibidas().contains(nombreAmigo)) {
                // por desarrollar (se añaden sus id en el apartado de amigos y eliminan las solicitudes
                if (MainActivity.usuario.getAmigos().equals("n")) {
                    MainActivity.databaseReferenceUsuario.child("amigos").setValue(nombreAmigo + "@");
                } else {
                    MainActivity.databaseReferenceUsuario.child("amigos").setValue(MainActivity.usuario.getAmigos() + nombreAmigo + "@");
                }

                if (usuarioAmigo.getAmigos().equals("n")) {
                    MainActivity.databaseReferenceAmigo.child("amigos").setValue(MainActivity.nombreUsuario + "@");
                } else {
                    MainActivity.databaseReferenceAmigo.child("amigos").setValue(usuarioAmigo.getAmigos() + MainActivity.nombreUsuario + "@");
                }
                MainActivity.databaseReferenceUsuario.child("solicitudesRecibidas").setValue(palabraEliminar(MainActivity.usuario.getAmigos(), (nombreAmigo + "@")));
                MainActivity.databaseReferenceAmigo.child("solicitudesEnviadas").setValue(palabraEliminar(usuarioAmigo.getAmigos(), (MainActivity.nombreUsuario + "@")));
            } else if (usuarioAmigo.getSolicitudesRecibidas().contains(MainActivity.nombreUsuario)) {
                //el usuario ya le envio la solicitud
                Toast.makeText(this, getResources().getString(R.string.errorPeticionExistente), Toast.LENGTH_SHORT).show();
            } else if (usuarioAmigo.getSolicitudesRecibidas().equals("n")) {
                //en el caso de que no haya
                MainActivity.databaseReferenceAmigo.child("solicitudesRecibidas").setValue(MainActivity.nombreUsuario + "@");
                if (MainActivity.usuario.getSolicitudesRecibidas().equals("n")) {
                    MainActivity.databaseReferenceUsuario.child("solicitudesEnviadas").setValue(nombreAmigo + "@");
                } else {
                    MainActivity.databaseReferenceUsuario.child("solicitudesEnviadas").setValue(MainActivity.usuario.getSolicitudesRecibidas() + nombreAmigo + "@");
                }
            } else {
                MainActivity.databaseReferenceAmigo.child("solicitudesRecibidas").setValue(usuarioAmigo.getSolicitudesEnviadas() + MainActivity.nombreUsuario + "@");
                if (MainActivity.usuario.getSolicitudesRecibidas().equals("n")) {
                    MainActivity.databaseReferenceUsuario.child("solicitudesEnviadas").setValue(nombreAmigo + "@");
                } else {
                    MainActivity.databaseReferenceUsuario.child("solicitudesEnviadas").setValue(MainActivity.usuario.getSolicitudesRecibidas() + nombreAmigo + "@");
                }
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.errorAmigoExistente), Toast.LENGTH_SHORT).show();
        }


        //OBLIGATORIO
        //Para el escaner
        zXingScannerView.resumeCameraPreview(this);
        //Apaga la camara
        zXingScannerView.stopCamera();

        //Cambia el activity
        setContentView(R.layout.activity_main);

    }

    public void generarQRPropio() {
        try {
            bitmap = TextToImageEncode(MainActivity.nombreUsuario);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        imageViewQRPropio.setImageBitmap(bitmap);
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    500, 500, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.common_google_signin_btn_text_light) : getResources().getColor(R.color.common_google_signin_btn_text_dark);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }


}
