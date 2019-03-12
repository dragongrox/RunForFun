package com.example.runforfun;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    static Usuario usuario;
    static FirebaseDatabase database;
    static DatabaseReference databaseReferenceUsuario;
    static DatabaseReference databaseReferenceAmigo;
    //firebase inicializacion
    static String nombreUsuario = "default";
    static boolean sonido = false;
    //pasos
    int pasos = -3,
            pasosDia,
            pasosActualizados = pasos;
    Sensor sensor;
    SensorEventListener andar;
    float velocidad = 0;

    //imagenGif
    int contImag = 0;
    ImageView imageView;

    //campos main
    FirebaseUser user;
    static FirebaseUser userCache;
    TextView textViewPasosDia,
            textViewPasos,
            textViewCalorias,
            textViewCaloriasDia,
            textViewDistancia,
            textViewDistanciaDia,
            textViewVelocidad;
    EditText editTextNombre,
            editTextAltura,
            editTextPeso;
    boolean mainActivo = true;
    //inicilizacion de sensores
    SensorManager sensorManager;

    //campos menu lateral
    List<String> listaAmigos;
    DrawerLayout drawerLayoutAmigos;
    RecyclerView recyclerViewAmigos;


    //formateador de los decimales
    DecimalFormat df;

    public static void EliminarAmigo(String nombreAmigo) {
        Usuario usuarioAmigo = new Usuario();
        sonido = true;
        databaseReferenceAmigo = database.getReference(nombreAmigo);
        databaseReferenceAmigo.addValueEventListener(new ValueEventListener() {
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
        if (EscanerQR.palabraEliminar(usuario.getAmigos(), nombreAmigo + "@").trim().isEmpty()) {
            databaseReferenceUsuario.child("amigos").setValue("n");
            databaseReferenceAmigo.child("amigos").setValue("n");
        } else {
            databaseReferenceUsuario.child("amigos").setValue(EscanerQR.palabraEliminar(usuario.getAmigos(), nombreAmigo + "@"));
            databaseReferenceAmigo.child("amigos").setValue(EscanerQR.palabraEliminar(usuarioAmigo.getAmigos(), nombreUsuario + "@"));
        }
    }

    public void ReproducirSonido() {
        //Sonidos
        MediaPlayer mediaPlayer;
        //inicializamos el media player para el sonido
        mediaPlayer = MediaPlayer.create(this, R.raw.omaewanani);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        sonido = false;
    }

    /**
     * aqui controlamos el cierre de la aplicacion
     */
    @Override
    protected void onStop() {
        databaseReferenceUsuario.setValue(usuario);
        database = null;
        mainActivo = false;
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (usuario != null && databaseReferenceUsuario != null)
            databaseReferenceUsuario.setValue(usuario);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //habilitamos la persistencia en el disco de firebase
        database = FirebaseDatabase.getInstance();
        databaseReferenceAmigo = database.getReference(nombreUsuario);

        //formateador de los decimales
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        //inicializacion de los campos
        textViewPasosDia = findViewById(R.id.textViewPasosDia);
        textViewCalorias = findViewById(R.id.textViewAmigoCalorias);
        textViewCaloriasDia = findViewById(R.id.textViewCaloriasDia);
        textViewPasos = findViewById(R.id.textViewAmigoPasos);
        textViewDistancia = findViewById(R.id.textViewDistancia);
        textViewDistanciaDia = findViewById(R.id.textViewDistanciaDia);
        textViewVelocidad = findViewById(R.id.textViewVelocidad);
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextAltura = findViewById(R.id.editTextAltura);
        editTextPeso = findViewById(R.id.editTextPeso);
        imageView = findViewById(R.id.imageView);

        FirebaseApp.initializeApp(this);

        //Obtencion del Usuario
        if (user == null) {
            if (userCache == null) {
                //Autentificacion
                createSignInIntent();

            } else {
                user = userCache;
            }

        }

        //Creacion y configuracion del sensor de podometro
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        //comprobamos que exista el sensor del podometro
        if (sensor == null) {
            Toast.makeText(this, "No tienes podómetro", Toast.LENGTH_LONG).show();
        }
        usuario = new Usuario();
        andar = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //cada vez que de un paso se sumaran los pasos
                pasos = (usuario.getPasos());
                pasosDia = (usuario.getPasosDia());
                pasos++;
                usuario.setPasos(pasos);
                pasosDia++;
                usuario.setPasosDia(pasosDia);
                textViewPasosDia.setText(getResources().getString(R.string.pasosDia) + ": " + pasosDia);
                textViewPasos.setText(getResources().getString(R.string.pasos) + ": " + pasos);
                textViewCalorias.setText(getResources().getString(R.string.calorias) + ": \t" + df.format(usuario.getCalorias()));
                textViewCaloriasDia.setText(getResources().getString(R.string.caloriasDia) + ": \t" + df.format(usuario.getCaloriasDia()));
                textViewDistancia.setText(getResources().getString(R.string.distancia) + ": " + df.format(usuario.getDistancia()));
                textViewDistanciaDia.setText(getResources().getString(R.string.distanciaDia) + ": " + df.format(usuario.getDistanciaDia()));

                //actualizamos la informacion en firebas cada 100 pasos y recalculamos las calorias
                if (pasos > (pasosActualizados + 100))
                    databaseReferenceUsuario.setValue(usuario);
                switch (contImag) {
                    case 0:
                        imageView.setImageResource(R.drawable.g0);
                        contImag++;
                        break;
                    case 1:
                        imageView.setImageResource(R.drawable.g1);
                        contImag++;
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.g2);
                        contImag++;
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.g3);
                        contImag++;
                        break;
                    case 4:
                        imageView.setImageResource(R.drawable.g4);
                        contImag++;
                        break;
                    case 5:
                        imageView.setImageResource(R.drawable.g5);
                        contImag = 0;
                        break;

                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        sensorManager.registerListener(andar, sensor, SensorManager.SENSOR_DELAY_GAME);

        new SincronizacionDatosAsyncTask().execute();

        dibujarListaAmigos();


    }

    public void dibujarListaAmigos() {
        String[] amigos = usuario.amigos.split("@");
        listaAmigos = Arrays.asList(amigos);
        System.out.println("asdasd");

        drawerLayoutAmigos = findViewById(R.id.drawer_layout);
        recyclerViewAmigos = findViewById(R.id.recyclerView);

        //creamos el adaptador para la lista
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerViewAmigos.setLayoutManager(llm);
        recyclerViewAmigos.setAdapter(new RVAdapter(listaAmigos));
    }

    /**
     * metodo para leer usuario.
     */
    public void leerUsuario() {
        //Extraemos el email y por lo consiguiente su id en la base de datos
        String[] emailPartido = user.getEmail().split("@");
        nombreUsuario = emailPartido[0];

        usuario = new Usuario();
        database = FirebaseDatabase.getInstance();
        databaseReferenceUsuario = database.getReference(nombreUsuario);

        databaseReferenceUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //extraer los datos del usuario de la base de datos
                    usuario.setAmigos(dataSnapshot.child("amigos").getValue().toString());
                    usuario.setCalorias(Float.parseFloat(dataSnapshot.child("calorias").getValue().toString()));
                    usuario.setCaloriasDia(Float.parseFloat(dataSnapshot.child("caloriasDia").getValue().toString()));
                    usuario.setNombre(dataSnapshot.child("nombre").getValue().toString());
                    usuario.setPasos(Integer.parseInt(dataSnapshot.child("pasos").getValue().toString()));
                    usuario.setPasosDia(Integer.parseInt(dataSnapshot.child("pasosDia").getValue().toString()));
                    usuario.setSolicitudesEnviadas(dataSnapshot.child("solicitudesEnviadas").getValue().toString());
                    usuario.setSolicitudesRecibidas(dataSnapshot.child("solicitudesRecibidas").getValue().toString());
                    usuario.setUltimaFecha(dataSnapshot.child("ultimaFecha").getValue().toString());
                    usuario.setAltura(Integer.parseInt(dataSnapshot.child("altura").getValue().toString()));
                    usuario.setPeso(Integer.parseInt(dataSnapshot.child("peso").getValue().toString()));
                    usuario.setDistancia(Double.parseDouble(dataSnapshot.child("distancia").getValue().toString()));
                    usuario.setDistanciaDia(Double.parseDouble(dataSnapshot.child("distanciaDia").getValue().toString()));

                    //mostrar los datos visibles
                    editTextNombre.setText(usuario.getNombre() + "");
                    editTextAltura.setText(usuario.getAltura() + "");
                    editTextPeso.setText(usuario.getPeso() + "");
                    textViewPasosDia.setText(getResources().getString(R.string.pasosDia) + ": " + usuario.getPasosDia());
                    textViewPasos.setText(getResources().getString(R.string.pasos) + ": " + usuario.getPasos());
                    textViewCalorias.setText(getResources().getString(R.string.calorias) + ": \t" + df.format(usuario.getCalorias()));
                    textViewCaloriasDia.setText(getResources().getString(R.string.caloriasDia) + ": \t" + df.format(usuario.getCaloriasDia()));
                    textViewDistancia.setText(getResources().getString(R.string.distancia) + ": " + df.format(usuario.getDistancia()));
                    textViewDistanciaDia.setText(getResources().getString(R.string.distanciaDia) + ": " + df.format(usuario.getDistanciaDia()));


                } else {
                    //creamos los datos del nuevo usuario usando los valores del usuario por defecto
                    databaseReferenceUsuario.setValue(usuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * Creacion del intent para el inicio de sesion
     */
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
                userCache = user;
                leerUsuario();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                System.exit(0);
            }
        }
    }

    /**
     * Desconecta de la cuenta
     *
     * @param view view del main
     */
    public void signOut(View view) {
        //guardamos los datos
        databaseReferenceUsuario.setValue(usuario);
        userCache = null;
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

    /**
     * Tema y logo.
     */
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

    /**
     * Eliminacion del usuario (no se usa)
     */
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

    /**
     * Metodo que se encarga de introducir datos personalizados
     */
    public void OnClicEditarCampos(View view) {
        boolean success = true;
        if (editTextNombre.isEnabled()) {
            if (editTextNombre.getText().toString().trim().length() > 0) {
                usuario.setNombre(editTextNombre.getText() + "");
            } else {
                Toast.makeText(this, getResources().getString(R.string.errorNombre), Toast.LENGTH_SHORT).show();
                success = false;
            }
            if (editTextAltura.getText().toString().trim().length() > 2) {
                usuario.setAltura(Integer.parseInt(editTextAltura.getText().toString().trim()));
            } else {
                Toast.makeText(this, getResources().getString(R.string.errorAltura), Toast.LENGTH_SHORT).show();
                success = false;
            }
            if (Integer.parseInt(editTextPeso.getText().toString().trim()) > 1) {
                usuario.setPeso(Integer.parseInt(editTextPeso.getText().toString().trim()));
            } else {
                Toast.makeText(this, getResources().getString(R.string.errorPeso), Toast.LENGTH_SHORT).show();
                success = false;
            }

            if (success) {
                databaseReferenceUsuario.setValue(usuario);
                editTextNombre.setEnabled(false);
                editTextAltura.setEnabled(false);
                editTextPeso.setEnabled(false);
            }
        } else {
            editTextPeso.setEnabled(true);
            editTextAltura.setEnabled(true);
            editTextNombre.setEnabled(true);
        }
    }

    /**
     * metodo que abre la vista web de la pagina del creador
     *
     * @param view
     */
    public void irAlSitioclick(View view) {
        Intent i = new Intent(this, WebActivity.class);
        startActivity(i);
    }

    /**
     * Privacidad y terminos
     */
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

    /**
     * On click aniadir amigo.
     *
     * @param view view del main
     */
    public void OnClickAniadirAmigo(View view) {
        Intent intentAniadirAmigo = new Intent(getApplicationContext(), EscanerQR.class);
        startActivity(intentAniadirAmigo);
    }

    /**
     * The type Sincronizacion datos async task.
     */
    public class SincronizacionDatosAsyncTask extends AsyncTask<String, Integer, Integer> {
        int pasosCache = pasos;
        //distancia en m que se recorre al dar un paso
        float multiplicadorPasos;

        @Override
        protected Integer doInBackground(String... strings) {
            do {
                try {
                    sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                velocidad = (((pasos - pasosCache) * 10) * multiplicadorPasos) * (0.06f * 3);
                usuario.setDistancia(usuario.getDistancia() + ((pasos - pasosCache) * multiplicadorPasos) / 1000);
                usuario.setDistanciaDia(usuario.getDistanciaDia() + ((pasos - pasosCache) * multiplicadorPasos) / 1000);
                if (velocidad >= 7) {
                    usuario.calorias += 0.048f * (usuario.getPeso() * 2.2) * 0.25f;
                    usuario.caloriasDia += 0.048f * (usuario.getPeso() * 2.2) * 0.25f;
                } else if (velocidad > 4) {
                    usuario.calorias += 0.029f * (usuario.getPeso() * 2.2) * 0.25f;
                    usuario.caloriasDia += 0.048f * (usuario.getPeso() * 2.2) * 0.25f;
                }
                pasosCache = pasos;
                publishProgress(1);
            } while (mainActivo);
            return 0;
        }

        @Override
        protected void onPreExecute() {
            if (usuario.altura > 170)
                multiplicadorPasos = 0.7f;
            else
                multiplicadorPasos = 0.6f;
        }

        @Override
        protected void onPostExecute(Integer integer) {

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //periodicamente actualizaremos la lista de amigos
            dibujarListaAmigos();
            //actualizamos la velocidad
            textViewVelocidad.setText(velocidad + "km/h");
            //comprobamos el cambio de dia para reiniciar las calorias, los pasos y la distancia de ese dia
            SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaAlmacenada = null, fechaActual = null;
            try {
                fechaAlmacenada = formatoDelTexto.parse(usuario.getUltimaFecha());
                fechaActual = formatoDelTexto.parse(new Usuario().getUltimaFecha());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (fechaActual.compareTo(fechaAlmacenada) != 0) {
                databaseReferenceUsuario.child("caloriasDia").setValue(0 + "");
                databaseReferenceUsuario.child("pasosDia").setValue(0 + "");
                databaseReferenceUsuario.child("distanciaDia").setValue(0 + "");
                databaseReferenceUsuario.child("ultimaFecha").setValue(new Usuario().getUltimaFecha());
            }

            if (sonido) {
                ReproducirSonido();
            }

        }
    }


}
