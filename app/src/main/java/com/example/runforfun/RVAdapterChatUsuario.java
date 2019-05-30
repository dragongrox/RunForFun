package com.example.runforfun;


import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class RVAdapterChatUsuario extends RecyclerView.Adapter<RVAdapterChatUsuario.PersonViewHolder> {
    //inicializamos la vista que contendra los items
    List listMensajes;

    DatabaseReference databaseReferenceChatUsuario;

    //inicializamos la vista con la vista que se nos pasa desde el main
    RVAdapterChatUsuario(List<String> listMensajes) {
        this.listMensajes = listMensajes;
    }

    //implementamos la superclase
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    //inicializamos el ViewHolder y especificamos el layout que usara nuestro RecyclerView para cada item
    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item_chat_usuario, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    //especificamos el contenido de cada item
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        //Conectamos a la base de datos para actualizar la informacion de las rutas
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceChatUsuario = database.getReference("Chat" + ChatUsuario.nombreUsuario + ChatUsuario.nombreAmigo);

        databaseReferenceChatUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    listMensajes = ((List) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Mensaje mensaje = new Mensaje((HashMap) listMensajes.get(i));
        DatabaseReference databaseReferenceMensaje = database.getReference(mensaje.autor);
        databaseReferenceMensaje.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                personViewHolder.textViewMensaje.setText(dataSnapshot.child("nombre").getValue().toString() + ":\n" + mensaje.texto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        if (mensaje.autor.equals(MainActivity.nombreUsuario)) {
            personViewHolder.linearLayout.setBackground(new ColorDrawable(0xFF69F0AE));
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(personViewHolder.linearLayout.getLayoutParams());
            ((LinearLayout.LayoutParams) layoutParams).setMargins(80, 0, 0, 0);
            personViewHolder.linearLayout.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        if (listMensajes != null)
            return listMensajes.size();
        else
            return 0;
    }

    //Creamos la clase que inicializa el view de nuestros items
    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        //declaramos los campos
        TextView textViewMensaje;
        LinearLayout linearLayout;

        //inicializamos los campos
        PersonViewHolder(View itemView) {
            super(itemView);
            textViewMensaje = itemView.findViewById(R.id.textViewMensaje);
            linearLayout = itemView.findViewById(R.id.cuadroMensaje);
        }


    }

}
