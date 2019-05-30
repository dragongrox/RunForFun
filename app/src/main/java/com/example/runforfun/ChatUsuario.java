package com.example.runforfun;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatUsuario extends AppCompatActivity {
    static DatabaseReference databaseReferenceChatUsuario;
    static DatabaseReference databaseReferenceChatUsuarioAmigo;
    static String nombreUsuario;
    static String nombreAmigo;
    List listaChat = new ArrayList<>();
    DrawerLayout drawerLayoutChat;
    RecyclerView recyclerViewChat;
    EditText editTextChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_usuario);

        editTextChat = findViewById(R.id.editTextChat);

        nombreUsuario = MainActivity.nombreUsuario;
        nombreAmigo = MainActivity.nombreUsuarioAmigo;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceChatUsuario = database.getReference("Chat" + nombreUsuario + nombreAmigo);
        databaseReferenceChatUsuarioAmigo = database.getReference("Chat" + nombreAmigo + nombreUsuario);

        MainActivity.nombreUsuarioAmigo = "";

        databaseReferenceChatUsuario.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listaChat = ((List) dataSnapshot.getValue());
                } else {
                    databaseReferenceChatUsuario.setValue(listaChat);
                }
                dibujarListaMensajes();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReferenceChatUsuarioAmigo.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listaChat = ((List) dataSnapshot.getValue());
                } else {
                    databaseReferenceChatUsuarioAmigo.setValue(listaChat);
                }
                dibujarListaMensajes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void onClickEnviarMensaje(View view) {

        String s = MainActivity.usuario.getNombre() + ": \n" + editTextChat.getText().toString();
        listaChat.add(new Mensaje(s, nombreUsuario));
        databaseReferenceChatUsuario.setValue(listaChat);
        databaseReferenceChatUsuarioAmigo.setValue(listaChat);
        editTextChat.setText("");
    }

    public void dibujarListaMensajes() {

        drawerLayoutChat = findViewById(R.id.drawer_layout_chat_usuario);
        recyclerViewChat = findViewById(R.id.recyclerViewChatUsuario);

        //creamos el adaptador para la lista
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerViewChat.setLayoutManager(llm);
        recyclerViewChat.setAdapter(new RVAdapterChatUsuario(listaChat));

    }
}


