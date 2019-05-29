package com.example.runforfun;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RVAdapterChatGlobal extends RecyclerView.Adapter<RVAdapterChatGlobal.PersonViewHolder> {
    //inicializamos la vista que contendra los items
    List<String> listMensajes;
    String[] contenidoChat;

    //inicializamos la vista con la vista que se nos pasa desde el main
    RVAdapterChatGlobal(List<String> listMensajes) {
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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item_chat_global, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    //especificamos el contenido de cada item
    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        //Conectamos a la base de datos para actualizar la informacion de las rutas
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceChatGlobal = database.getReference("ChatGeneral");
        databaseReferenceChatGlobal.addValueEventListener(new ValueEventListener() {
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

        personViewHolder.textViewMensaje.setText(listMensajes.get(i));
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

        //inicializamos los campos
        PersonViewHolder(View itemView) {
            super(itemView);
            textViewMensaje = itemView.findViewById(R.id.textViewMensaje);
        }


    }

}
