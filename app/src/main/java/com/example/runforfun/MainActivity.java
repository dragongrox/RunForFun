package com.example.runforfun;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.model.LatLng;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    static Usuario usuario;
    static Usuario usuarioAmigo;
    static FirebaseDatabase database;
    static DatabaseReference databaseReferenceUsuario;
    static DatabaseReference databaseReferenceAmigo;
    //firebase inicializacion
    static String nombreUsuario = "default";
    static String nombreUsuarioAmigo = "";
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

    //Declaracion de los AsyncTask
    SincronizacionDatosBiometricosAsyncTask sincronizacionDatosBiometricosAsyncTask;

    //formateador de los decimales
    DecimalFormat df;

    //margen de error del gps
    static final double MARGEN_ERROR = 0.0002d;

    //variable semaforo que controla la finalizacion del AsyncTask
    boolean asyncTaskTerminado = false;
    //vriable semaform que controla la lectura de los datos
    boolean usuarioLeido = false;

    /**
     * metodo que se encarga de eliminar el amigo que tenga el nombre pasado por parametro
     *
     * @param nombreAmigo nombre del amigo
     */
    public static void EliminarAmigo(String nombreAmigo) {
        usuarioAmigo = new Usuario();
        //marcamos el semaforo para reproducir el sonido que en la siguiente actualizacion del AsyncTask emitira el sonido y actualizara el recycled view del menu lateral
        sonido = true;
        //obtenemos al nodo del amigo
        databaseReferenceAmigo = database.getReference(nombreAmigo);
        //inicializamos los listeners para leer sus campos y asi detectar solo el nombre del amigo que queramos borrar
        databaseReferenceAmigo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //obtenemos los campos y los almacenamos en el objeto usuarioAmigo
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
                usuarioAmigo.setPosiciones((ArrayList<Posicion>) dataSnapshot.child("posiciones").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //comprobamos si este es el ultimo amigo que tenemos o quedan mas, para no dejar el campo vacio y poner el caracter de n señalando que eeste campo esta vacio
        if (EscanerQR.palabraEliminar(usuario.getAmigos(), nombreAmigo + "@").trim().isEmpty()) {
            databaseReferenceUsuario.child("amigos").setValue("n");
            databaseReferenceAmigo.child("amigos").setValue("n");
        } else {
            databaseReferenceUsuario.child("amigos").setValue(EscanerQR.palabraEliminar(usuario.getAmigos(), nombreAmigo + "@"));
            databaseReferenceAmigo.child("amigos").setValue(EscanerQR.palabraEliminar(usuarioAmigo.getAmigos(), nombreUsuario + "@"));
        }
    }

    /**
     * metodo que reproduce sonido al invocarlo
     */
    public void ReproducirSonido() {
        //Sonidos
        MediaPlayer mediaPlayer;
        //inicializamos el media player para el sonido
        mediaPlayer = MediaPlayer.create(this, R.raw.omaewanani);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        sonido = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 22: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Etapa del ciclo de la vida que corresponde al inicio de este Activity
     *
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

        FirebaseApp.initializeApp(this);

        //permisos de Localizacion
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        22);

                // ACCESS_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        //Obtencion del Usuario
        if (user == null) {
            if (userCache == null) {
                createSignInIntent();


                //Creacion y configuracion del sensor de podometro
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

                //comprobamos que exista el sensor del podometro
                if (sensor == null) {
                    Toast.makeText(this, "No tienes podómetro", Toast.LENGTH_LONG).show();
                }
                //inicializacion del usuario
                if (usuario == null)
                    usuario = new Usuario();
                //inicializacion del sensor de podometro
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

                        usuario = usuario;

                        //actualizamos la informacion en firebas cada 100 pasos y recalculamos las calorias
                        if (pasos > (pasosActualizados + 20))
                            databaseReferenceUsuario.setValue(usuario);

                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
                //puesta en marcha del sensor podometro
                sensorManager.registerListener(andar, sensor, SensorManager.SENSOR_DELAY_GAME);


            }
        }
    }


    public void dibujarListaAmigos() {
        String[] amigos = usuario.amigos.split("@");
        listaAmigos = Arrays.asList(amigos);

        drawerLayoutAmigos = findViewById(R.id.drawer_layout);
        recyclerViewAmigos = findViewById(R.id.recyclerView);

        //creamos el adaptador para la lista
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerViewAmigos.setLayoutManager(llm);
        recyclerViewAmigos.setAdapter(new RVAdapterListaAmigos(listaAmigos));

    }

    /**
     * metodo para leer usuario.
     */
    public void leerUsuario() {
        //Extraemos el email y por lo consiguiente su id en la base de datos
        String[] emailPartido = user.getEmail().split("@");
        nombreUsuario = emailPartido[0];

        //inicializamos todo_lo referente a la base de datos firebase
        usuario = new Usuario();
        database = FirebaseDatabase.getInstance();
        databaseReferenceUsuario = database.getReference(nombreUsuario);
        //creamos los listeners para leer los datos
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
                    usuario.setPosiciones((ArrayList<Posicion>) dataSnapshot.child("posiciones").getValue());


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

                    usuarioLeido = true;
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

    /**
     * metodo que recibe el resultado del Activity de inicio de sesion
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Si se ha iniciado correctamente pasamos el id a la variable user
                user = FirebaseAuth.getInstance().getCurrentUser();
                // tambien pasamos esta al cache que se encargara de mantener nuestro usuario entre cambios de layouts debido al giro de la pantalla
                userCache = user;
                //actualizamos los datos del usuario en la parte grafica
                leerUsuario();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Actualizamos la lista de migos
                                    String[] amigos = usuario.amigos.split("@");

                                    if (listaAmigos == null) {
                                        dibujarListaAmigos();
                                    }
                                    if (listaAmigos.get(0) == "n" || Arrays.asList(amigos).get(0) == "n" || listaAmigos.size() != Arrays.asList(amigos).size()) {
                                        dibujarListaAmigos();
                                    }
                                    //Abertura del chat con el amigo
                                    if (!nombreUsuarioAmigo.isEmpty()) {
                                        OnClickChatUsuario();
                                    }

                                    //si el boolean que se encarga de anunciar la eliminacion de un amigo esta en true se procedera a hacer sonar el sonido
                                    if (sonido) {
                                        ReproducirSonido();
                                    }

                                    //actualizacion de la informacion cada 2 seg si el usuario ya ha sido leido
                                    if (usuarioLeido) {
                                        databaseReferenceUsuario.child("amigos").setValue(usuario.amigos);
                                        databaseReferenceUsuario.child("calorias").setValue(usuario.calorias);
                                        databaseReferenceUsuario.child("caloriasDia").setValue(usuario.caloriasDia);
                                        databaseReferenceUsuario.child("nombre").setValue(usuario.nombre);
                                        databaseReferenceUsuario.child("pasos").setValue(usuario.pasos);
                                        databaseReferenceUsuario.child("pasosDia").setValue(usuario.pasosDia);
                                        databaseReferenceUsuario.child("solicitudesEnviadas").setValue(usuario.solicitudesEnviadas);
                                        databaseReferenceUsuario.child("solicitudesRecibidas").setValue(usuario.solicitudesRecibidas);
                                        databaseReferenceUsuario.child("ultimaFecha").setValue(usuario.ultimaFecha);
                                        databaseReferenceUsuario.child("altura").setValue(usuario.altura);
                                        databaseReferenceUsuario.child("peso").setValue(usuario.peso);
                                        databaseReferenceUsuario.child("distancia").setValue(usuario.distancia);
                                        databaseReferenceUsuario.child("distanciaDia").setValue(usuario.distanciaDia);
                                        databaseReferenceUsuario.child("posiciones").setValue(usuario.posiciones);

                                        databaseReferenceUsuario.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
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
                                                    usuario.setPosiciones((ArrayList<Posicion>) dataSnapshot.child("posiciones").getValue());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        leerUsuario();
                                    }



                                    //Actualizacion y guardado de la posicion GPS
                                    LatLng posicion = ObtenerPosicion();
                                    try {
                                        double lat = new Posicion((Map<String, String>) usuario.posiciones.get(usuario.posiciones.size() - 1)).lat;
                                        double lon = new Posicion((Map<String, String>) usuario.posiciones.get(usuario.posiciones.size() - 1)).lon;
                                        if (lat - posicion.latitude > MARGEN_ERROR || lat - posicion.latitude < -MARGEN_ERROR
                                                || lon - posicion.longitude > MARGEN_ERROR || lon - posicion.longitude < -MARGEN_ERROR || usuario.posiciones.size() < 1) {
                                            usuario.posiciones.add(new Posicion(posicion.latitude, posicion.longitude, usuario.ultimaFecha));
                                            databaseReferenceUsuario.child("posiciones").setValue(usuario.posiciones);
                                        }
                                    } catch (NullPointerException e) {
                                        usuario.posiciones = new ArrayList<>();
                                        usuario.posiciones.add(new Posicion(posicion.latitude, posicion.longitude, usuario.ultimaFecha));
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        usuario.posiciones = new ArrayList<>();
                                        usuario.posiciones.add(new Posicion(posicion.latitude, posicion.longitude, usuario.ultimaFecha));
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                    MainActivity.usuario = usuario;

                                    System.out.println("El servicio de actualizacion esta activo...");
                                }
                            });

                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (true);

                    }
                }).start();

                sincronizacionDatosBiometricosAsyncTask = new SincronizacionDatosBiometricosAsyncTask();
                sincronizacionDatosBiometricosAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            } else {
                //salimos si no se ha iniciado sesion
                System.exit(0);
            }
        }
    }

    /**
     * Metodo que actualiza al amigo que hay que abrir el chat privado
     *
     * @param nombreUsuarioAmigo
     */
    public static void OnClickChatUsuario(String nombreUsuarioAmigo) {
        MainActivity.nombreUsuarioAmigo = nombreUsuarioAmigo;

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
                editTextNombre.setTextColor(Color.WHITE);
                editTextAltura.setEnabled(false);
                editTextAltura.setTextColor(Color.WHITE);
                editTextPeso.setEnabled(false);
                editTextPeso.setTextColor(Color.WHITE);
            }
        } else {
            editTextPeso.setEnabled(true);
            editTextPeso.setTextColor(Color.CYAN);
            editTextAltura.setEnabled(true);
            editTextAltura.setTextColor(Color.CYAN);
            editTextNombre.setEnabled(true);
            editTextNombre.setTextColor(Color.CYAN);
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
     * metod que muestra los terminos a los que estan sujetos los usuarios respecto a su privacidad
     */
    public void privacyAndTerms(View view) {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();
        // [START auth_fui_pp_tos]
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "https://policies.google.com/terms?hl=es-419",
                                "https://policies.google.com/terms?hl=es-419")
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_pp_tos]
    }

    /**
     * Metodo para obtener la posicion actual GPS
     *
     * @return
     */
    public LatLng ObtenerPosicion() {
        //Inicializamos un location manager para poder obtener la posicion de el
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Comprobamos los permisos del GPS o su existencia y pedimos permiso si hace falta
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            //En el caso de no obtener el permiso o no tener el dispositivo un sensor GPS se devuelve null
            return null;
        }
        //Obtenermos la posicion con el metodo getLastKnownLocation
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //inicializamos el objeto de posicion
        LatLng posicion = new LatLng(0, 0);
        //comprobando que el objeto location no sea nulo almacenamos la longitud y latitud dentro posicion
        if (location != null) {
            double latitud = location.getLatitude();
            double longitud = location.getLongitude();
            posicion = new LatLng(latitud, longitud);
        }
        //devolvemos el objeto posicion
        return posicion;
    }

    /**
     * On click para abrir el activity de aniadir amigo.
     *
     * @param view view del main
     */
    public void OnClickAniadirAmigo(View view) {
        Intent intentAniadirAmigo = new Intent(getApplicationContext(), EscanerQR.class);
        startActivity(intentAniadirAmigo);
        leerUsuario();
    }

    /**
     * Desconecta de la cuenta
     *
     * @param view view del main
     */
    public void signOut(View view) {
        //guardamos los datos
        databaseReferenceUsuario.setValue(usuario);
        //eliminamos el cache
        userCache = null;
        // [START auth_fui_signout]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        createSignInIntent();
                    }
                });
        createSignInIntent();
        // [END auth_fui_signout]

    }

    /**
     * On click para abrir la ventana del mapa de ruta
     *
     * @param view
     */
    public void OnClicMaps(View view) {
        Intent intentGoogleMaps = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intentGoogleMaps);
    }

    /**
     * On click para abrir la ventana del chat General / Global
     */
    public void OnClicChatGlobal(View view) {
        Intent intentChatGlobal = new Intent(getApplicationContext(), ChatGeneral.class);
        intentChatGlobal.putExtra("nombreUsuario", usuario.getNombre());
        startActivity(intentChatGlobal);

    }

    /**
     * metodo que abre la ventana del chat privado
     */
    public void OnClickChatUsuario() {
        Intent intentChatUsuario = new Intent(getApplicationContext(), ChatUsuario.class);
        startActivity(intentChatUsuario);
    }

    /**
     * The type Sincronizacion datos async task.
     */
    public class SincronizacionDatosBiometricosAsyncTask extends AsyncTask<String, Integer, Integer> {
        int pasosCache = pasos;
        //distancia en m que se recorre al dar un paso
        float multiplicadorPasos;

        /**
         * Este metodo servira para hacer reiteradas actualizaciones sobre los dados del usuario
         *
         * @param strings
         * @return
         */
        @Override
        protected Integer doInBackground(String... strings) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    do {

                        try {
                            sleep(6000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                publishProgress(1);
                            }
                        });
                    } while (true);

                }
            }).start();
            return 1;
        }

        /**
         * metodo que se encarga de preparar la informacion necesario (el multiplicador que va en base a la altura del usuario)
         */
        @Override
        protected void onPreExecute() {
            if (usuario.altura > 170)
                multiplicadorPasos = 0.7f;
            else
                multiplicadorPasos = 0.6f;
            asyncTaskTerminado = false;
            //puesta en marcha del sensor podometro
            sensorManager.registerListener(andar, sensor, SensorManager.SENSOR_DELAY_GAME);
        }

        /**
         * Tareas tras finalizar el AsyncTask, en nuestro caso indicamos en la variable semaforo que el asinctask se ha terminado
         *
         * @param integer
         */
        @Override
        protected void onPostExecute(Integer integer) {
            asyncTaskTerminado = true;
        }

        /**
         * este apartado se encargara de actualizar demas datos de la propia interfaz, ya que el metodo do in background no tiene acceso a la interfaz
         *
         * @param values valor de progreso
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            //Aqui se calculan los distintos datos biometricos
            velocidad = (((pasos - pasosCache) * 10) * multiplicadorPasos) * (0.06f * 3);
            usuario.setDistancia(usuario.getDistancia() + ((pasos - pasosCache) * multiplicadorPasos) / 1000);
            usuario.setDistanciaDia(usuario.getDistanciaDia() + ((pasos - pasosCache) * multiplicadorPasos) / 1000);
            if (velocidad >= 3) {
                usuario.calorias += 0.048f * (usuario.getPeso() * 2.2f) * 0.25f;
                usuario.caloriasDia += 0.048f * (usuario.getPeso() * 2.2f) * 0.25f;
            } else if (velocidad >= 1) {
                usuario.calorias += 0.029f * (usuario.getPeso() * 2.2f) * 0.25f;
                usuario.caloriasDia += 0.048f * (usuario.getPeso() * 2.2f) * 0.25f;
            }
            pasosCache = pasos;

            //actualizamos la velocidad
            textViewVelocidad.setText(velocidad + "km/h");
            //comprobamos el cambio de dia para reiniciar las calorias, los pasos y la distancia de ese dia
            //primero convertimos las fechas en formato Date
            SimpleDateFormat formatoDelTexto = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaAlmacenada = null, fechaActual = null;
            try {
                fechaAlmacenada = formatoDelTexto.parse(usuario.getUltimaFecha());
                fechaActual = formatoDelTexto.parse(new Usuario().getUltimaFecha());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //hacemos la comparacion
            if (fechaActual.compareTo(fechaAlmacenada) != 0 && fechaActual != null && fechaAlmacenada != null) {
                databaseReferenceUsuario.child("caloriasDia").setValue(0 + "");
                databaseReferenceUsuario.child("pasosDia").setValue(0 + "");
                databaseReferenceUsuario.child("distanciaDia").setValue(0 + "");
                databaseReferenceUsuario.child("ultimaFecha").setValue(new Usuario().getUltimaFecha());
            }


            System.out.println("El servicio biometrico esta activo....");

        }
    }


}
