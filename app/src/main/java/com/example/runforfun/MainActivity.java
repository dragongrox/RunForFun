package com.example.runforfun;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static String usuario = "default@gmail.com";
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener andar;
    int pasos = -3;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creacion y configuracion del sensor de podometro

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        textView = findViewById(R.id.textViewPasos);

        if (sensor == null) {
            Toast.makeText(this, "No tienes podómetro", Toast.LENGTH_LONG).show();
        }

        andar = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                pasos++;
                textView.setText(pasos + "");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(andar, sensor, SensorManager.SENSOR_DELAY_GAME);

    }

    public void OnClickAniadirAmigo(View view) {
        Intent intentAniadirAmigo = new Intent(getApplicationContext(), EscanerQR.class);
        startActivity(intentAniadirAmigo);
    }


}
