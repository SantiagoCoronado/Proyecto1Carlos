package com.example.chronos.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    String IDUser, FullNameUser;

    public void logInAccount(View view) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        final boolean[] userMatches = {false};
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Map<String, Object> userValues = (Map<String, Object>) data.getValue();
                        String userEmail = userValues.get("Correo").toString();
                        String userPassword = userValues.get("Contrasenia").toString();
                        if (userEmail.equals(emailEditText.getText().toString()) && userPassword.equals(passwordEditText.getText().toString())) {
                            userMatches[0] = true;
                            IDUser = userValues.get("Nombre").toString() + userValues.get("ApellidoPat").toString() + userValues.get("ApellidoMat").toString();
                            FullNameUser = userValues.get("Nombre").toString() + " " + userValues.get("ApellidoPat").toString() + " " + userValues.get("ApellidoMat").toString();
                        }
                    }
                    if (userMatches[0]) {
                        Intent intent = new Intent(getApplicationContext(), MainMenu.class);
                        intent.putExtra("IDUser", IDUser);
                        intent.putExtra("FullNameUser", FullNameUser);
                        startActivity(intent);
                    } else {
                        new AlertDialog.Builder(LogInActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(getString(R.string.wrong_credentials))
                                .setMessage(getString(R.string.wrong_credentials_detail))
                                .setPositiveButton(getString(R.string.ok_option), null)
                                .show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
    }
}
