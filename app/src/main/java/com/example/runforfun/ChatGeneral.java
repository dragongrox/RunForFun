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

public class ChatGeneral extends AppCompatActivity {
    List<String> listaChat = new ArrayList<>();
    DrawerLayout drawerLayoutChat;
    RecyclerView recyclerViewChat;
    EditText editTextChat;

    DatabaseReference databaseReferenceChatGlobal;
    String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_general);

        editTextChat = findViewById(R.id.editTextChat);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceChatGlobal = database.getReference("ChatGeneral");

        nombreUsuario = getIntent().getStringExtra("nombreUsuario");

        databaseReferenceChatGlobal.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listaChat = ((List) dataSnapshot.getValue());
                } else {
                    //creamos los datos del nuevo usuario usando los valores del usuario por defecto
                    databaseReferenceChatGlobal.setValue(listaChat);
                }

                dibujarListaMensajes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onClickEnviarMensaje(View view) {

        String s = nombreUsuario + ": \n" + editTextChat.getText().toString();
        listaChat.add(s);
        databaseReferenceChatGlobal.setValue(listaChat);
    }

    public void dibujarListaMensajes() {

        drawerLayoutChat = findViewById(R.id.drawer_layout_chat_general);
        recyclerViewChat = findViewById(R.id.recyclerViewChatGeneral);

        //creamos el adaptador para la lista
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerViewChat.setLayoutManager(llm);
        recyclerViewChat.setAdapter(new RVAdapterChatGlobal(listaChat));

    }
}


