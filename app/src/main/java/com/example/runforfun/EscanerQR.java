package com.example.runforfun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

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

    @Override
    public void handleResult(Result result) {
        //Creamos caja de dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Titulo de la caja de dialogo
        builder.setTitle("Resultado");
        //Manejamos el resultado
        if (result.getText().equals("8480000725646")) {
            builder.setMessage("Codigo de amigo escaneado");
        } else if (result.getText().contains("http")) {//si es una URL
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(result.getText()));
            //Abre el navegador
            startActivity(i);
        } else {//si otras cosas son escaneadas
            builder.setMessage(result.getText());
        }

        AlertDialog mensaje = builder.create();
        mensaje.show();

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
