package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference database;
    private EditText editTextDato;
    private Button buttonGuardar;
    private TextView textViewDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Conectar a Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference("mensajes");

        // 🧩 Vincular los elementos del layout
        editTextDato = findViewById(R.id.editTextDato);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        textViewDatos = findViewById(R.id.textViewDatos);

        // 📝 Guardar datos al presionar el botón
        buttonGuardar.setOnClickListener(v -> {
            String texto = editTextDato.getText().toString().trim();

            if (texto.isEmpty()) {
                Toast.makeText(MainActivity.this, "Escribe algo primero", Toast.LENGTH_SHORT).show();
            } else {
                // 🔹 push() crea una clave única para cada mensaje
                String id = database.push().getKey();

                if (id != null) {
                    database.child(id).setValue(texto)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MainActivity.this, "Mensaje guardado", Toast.LENGTH_SHORT).show();
                                editTextDato.setText("");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(MainActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });

        // 👀 Escuchar todos los mensajes en tiempo real
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder mensajes = new StringBuilder();

                for (DataSnapshot dato : snapshot.getChildren()) {
                    String texto = dato.getValue(String.class);
                    mensajes.append("• ").append(texto).append("\n");
                }

                textViewDatos.setText(mensajes.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
