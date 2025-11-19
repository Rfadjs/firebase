package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    // 🔥 AUTH
    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private Button buttonRegistrar, buttonLogin;

    // 🔥 REALTIME DATABASE
    private DatabaseReference database;
    private EditText editTextDato;
    private Button buttonGuardar;
    private TextView textViewDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // -----------------------------------
        // 🔥 AUTENTICACIÓN
        // -----------------------------------
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonRegistrar.setOnClickListener(v -> registrarUsuario());
        buttonLogin.setOnClickListener(v -> iniciarSesion());

        // -----------------------------------
        // 🔥 BASE DE DATOS
        // -----------------------------------
        database = FirebaseDatabase.getInstance().getReference("mensajes");

        editTextDato = findViewById(R.id.editTextDato);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        textViewDatos = findViewById(R.id.textViewDatos);

        // Guardar mensaje
        buttonGuardar.setOnClickListener(v -> guardarMensaje());

        // Escuchar la base de datos
        cargarMensajesEnTiempoReal();
    }

    // -----------------------------------
    // 🔥 MÉTODOS AUTENTICACIÓN
    // -----------------------------------

    private void registrarUsuario() {
        String email = editTextEmail.getText().toString().trim();
        String pass = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void iniciarSesion() {
        String email = editTextEmail.getText().toString().trim();
        String pass = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // -----------------------------------
    // 🔥 GUARDAR DATOS SOLO SI HAY LOGIN
    // -----------------------------------
    private void guardarMensaje() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Primero inicia sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String texto = editTextDato.getText().toString().trim();

        if (texto.isEmpty()) {
            Toast.makeText(MainActivity.this, "Escribe algo primero", Toast.LENGTH_SHORT).show();
        } else {
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
    }

    // -----------------------------------
    // 🔥 LEER DATOS EN TIEMPO REAL
    // -----------------------------------
    private void cargarMensajesEnTiempoReal() {
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
