package com.example.runforfun;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static String nombreUsuario = "default";
    int calorias, caloriasDia, pasos = -3, pasosDia, pasosActualizados = pasos;
    Date ultimaFecha;

    Usuario usuario;

    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener andar;

    TextView textViewPasosDia, textViewPasos, textViewCalorias, textViewCaloriasDia;
    EditText editTextNombre;

    private static final int RC_SIGN_IN = 123;

    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference databaseReferenceUsuario;


    @Override
    protected void onStop() {
        databaseReferenceUsuario.setValue(usuario);
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inicializacion de los campos
        textViewPasosDia = findViewById(R.id.textViewPasosDia);
        textViewCalorias = findViewById(R.id.textViewCalorias);
        textViewCaloriasDia = findViewById(R.id.textViewCaloriasDia);
        textViewPasos = findViewById(R.id.textViewPasos);
        editTextNombre = findViewById(R.id.editTextNombre);

        //Autentificacion

        FirebaseApp.initializeApp(this);
        createSignInIntent();

        //Obtencion del Usuario
        if (user == null) {
            //en el caso de que aun no este inicializado y la cuenta ya este iniciada volvemos a instanciar al Usuario
            user = FirebaseAuth.getInstance().getCurrentUser();
        }

        //Creacion y configuracion del sensor de podometro

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (sensor == null) {
            Toast.makeText(this, "No tienes podómetro", Toast.LENGTH_LONG).show();
        }
        usuario = new Usuario();
        andar = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                pasos = (usuario.getPasos());
                pasosDia = (usuario.getPasos());
                pasos++;
                usuario.setPasos(pasos);
                pasosDia++;
                usuario.setPasosDia(pasosDia);
                textViewPasosDia.setText(getResources().getString(R.string.pasosDia) + ": " + pasosDia);
                textViewPasos.setText(getResources().getString(R.string.pasos) + ": " + pasos);
                if (pasos > (pasosActualizados + 100))
                    databaseReferenceUsuario.setValue(usuario);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(andar, sensor, SensorManager.SENSOR_DELAY_GAME);
    }


    public void leerUsuario() {
        //Extraemos el email y por lo consiguiente su id en la base de datos
        String[] emailPartido = user.getEmail().split("@");
        nombreUsuario = emailPartido[0];

        database = FirebaseDatabase.getInstance();
        databaseReferenceUsuario = database.getReference(nombreUsuario);
        databaseReferenceUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    usuario.setAmigos(dataSnapshot.child("amigos").getValue().toString());
                    usuario.setCalorias(Integer.parseInt(dataSnapshot.child("calorias").getValue().toString()));
                    usuario.setCaloriasDia(Integer.parseInt(dataSnapshot.child("caloriasDia").getValue().toString()));
                    usuario.setNombre(dataSnapshot.child("nombre").getValue().toString());
                    usuario.setPasos(Integer.parseInt(dataSnapshot.child("pasos").getValue().toString()));
                    usuario.setPasosDia(Integer.parseInt(dataSnapshot.child("pasosDia").getValue().toString()));
                    usuario.setSolicitudesEnviadas(dataSnapshot.child("solicitudesEnviadas").getValue().toString());
                    usuario.setSolicitudesRecibidas(dataSnapshot.child("solicitudesRecibidas").getValue().toString());
                    usuario.setUltimaFecha(dataSnapshot.child("ultimaFecha").getValue().toString());
                    editTextNombre.setText(usuario.getNombre());
                    textViewPasosDia.setText(getResources().getString(R.string.pasosDia) + ": " + usuario.getPasosDia());
                    textViewPasos.setText(getResources().getString(R.string.pasos) + ": " + usuario.getPasos());
                    System.out.println("adsadad");
                } else {
                    databaseReferenceUsuario.setValue(usuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]

    }

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                leerUsuario();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    public void signOut(View view) {
        //guardamos los datos
        databaseReferenceUsuario.setValue(usuario);
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        createSignInIntent();
                    }
                });
        // [END auth_fui_signout]
    }

    public void themeAndLogo() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_theme_logo]
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.logo)      // Set logo drawable
                        .setTheme(R.style.AppTheme)      // Set theme
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_theme_logo]
    }

    public void delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
        // [END auth_fui_delete]
    }

    public class SincronizacionDatosAsyncTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {

            return 0;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Integer integer) {

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }

    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();
        // [START auth_fui_pp_tos]
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "https://example.com/terms.html",
                                "https://example.com/privacy.html")
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_pp_tos]
    }

    public void OnClickAniadirAmigo(View view) {
        Intent intentAniadirAmigo = new Intent(getApplicationContext(), EscanerQR.class);
        startActivity(intentAniadirAmigo);
    }


}
