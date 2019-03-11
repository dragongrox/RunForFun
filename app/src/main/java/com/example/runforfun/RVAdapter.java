package com.example.runforfun;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import static java.lang.Thread.sleep;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {
    //inicializamos la vista que contendra los items
    List<String> listAmigos;
    Usuario usuario = new Usuario();

    //inicializamos la vista con la vista que se nos pasa desde el main
    RVAdapter(List<String> listAmigos) {
        this.listAmigos = listAmigos;
    }

    //implementamos la superclase
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //inicializamos el ViewHolder y especificamos el layout que usara nuestro RecyclerView para cada item
    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    //especificamos el contenido de cada item
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        if (!listAmigos.get(i).equals("n")) {
            //Conectamos a la base de datos para actualizar la informacion de las rutas
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReferenceUsuario = database.getReference(listAmigos.get(i));
            databaseReferenceUsuario.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                    personViewHolder.textViewAmigoCalorias.setText(df.format(usuario.getCaloriasDia()) + "");
                    personViewHolder.textViewAmigoPasos.setText(usuario.getPasos() + "");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            personViewHolder.imageViewAmigo.setImageResource(R.drawable.logo);
            personViewHolder.textViewAmigo.setText(listAmigos.get(i));
            personViewHolder.imageButtonBorrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.EliminarAmigo(personViewHolder.textViewAmigo.getText().toString() + "");
                }
            });
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            personViewHolder.textViewAmigo.setText(R.string.errorNoTienesAmigos);
            personViewHolder.textViewAmigoCalorias.setText("");
            personViewHolder.textViewAmigoPasos.setText("");
            personViewHolder.textViewAmigoCaloriasString.setText("");
            personViewHolder.textViewAmigoPasosString.setText("");
            personViewHolder.imageButtonBorrar.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public int getItemCount() {
        return listAmigos.size();
    }

    //reamos la clase que inicializa el view de nuestros items
    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        //declaramos los campos
        ImageView imageViewAmigo;
        TextView textViewAmigo, textViewAmigoCalorias, textViewAmigoPasos, textViewAmigoCaloriasString, textViewAmigoPasosString;
        ImageButton imageButtonBorrar;

        //inicializamos los campos
        PersonViewHolder(View itemView) {
            super(itemView);
            imageViewAmigo = itemView.findViewById(R.id.imageViewAmigo);
            textViewAmigo = itemView.findViewById(R.id.textViewAmigo);
            imageButtonBorrar = itemView.findViewById(R.id.imageButtonBorrar);
            textViewAmigoCalorias = itemView.findViewById(R.id.textViewAmigoCalorias);
            textViewAmigoPasos = itemView.findViewById(R.id.textViewAmigoPasos);
            textViewAmigoCaloriasString = itemView.findViewById(R.id.textViewAmigoCaloriasString);
            textViewAmigoPasosString = itemView.findViewById(R.id.textViewAmigoPasosString);
        }


    }

}
