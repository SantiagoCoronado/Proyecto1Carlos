package com.example.chronos.finalproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Register2ndActivity extends AppCompatActivity {

    Bitmap profilePic;
    ImageView imageView;
    String name, lastName, mLastName, email, password, birthDay, birthMonth, birthYear, IDUser;

    // Al intentar subir una foto revisar permiso de lectura, si no hay pedirlos, si hay ejecutar metodo getPhoto()
    public void selectPicture(View view) {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            getPhoto();
        }
    }

    // Resultado de permisos para subir foto de perfil, en caso de negarse mostrar mensaje
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPhoto();
            } else {
                Toast.makeText(this, getString(R.string.ask_for_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Lanzar actividad de fotos almacenadas para seleccionar foto de perfil, si selecciono una el usuario mostrar en imageView
    public void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                profilePic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageView.setImageBitmap(profilePic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo de conversion de bitMap a String para subir a la base de datos
    public String bitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    // Cancelar registro y volver a login preguntando en un display alert
    public void cancelRegister(View view) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.cancel_message))
                .setMessage(getString(R.string.cancel_message_detail))
                .setPositiveButton(getString(R.string.accept_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Register2ndActivity.this, LogInSignUpOptionsActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.decline_message), null)
                .show();
    }

    public void finishRegister(View view) {
        if (profilePic == null) {
            profilePic = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_picture);
        }
        DatabaseReference rootUsersRef = FirebaseDatabase.getInstance().getReference("Usuarios");
        IDUser = name + lastName + mLastName;
        DatabaseReference newUserRef = rootUsersRef.child(IDUser);
        Map<String, Object> valuesToSet = new HashMap<>();
        valuesToSet.put("Rol", "Usuario");
        valuesToSet.put("Nombre", name);
        valuesToSet.put("ApellidoPat", lastName);
        valuesToSet.put("ApellidoMat", mLastName);
        valuesToSet.put("DiaNac", birthDay);
        valuesToSet.put("MesNac", birthMonth);
        valuesToSet.put("AnioNac", birthYear);
        valuesToSet.put("Correo", email);
        valuesToSet.put("Contrasenia", password);
        newUserRef.updateChildren(valuesToSet);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios-FotosPerfil/" + name + lastName + mLastName + "/");
        Map<String, Object> photoUpdate = new HashMap<>();
        photoUpdate.put("fotoPerfil", bitMapToString(profilePic));
        userRef.updateChildren(photoUpdate);

        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("IDUser", IDUser);
        intent.putExtra("FullNameUser", name + " " + lastName + " " + mLastName);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2nd);

        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        lastName = intent.getStringExtra("lastName");
        mLastName = intent.getStringExtra("mLastName");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
        birthDay = intent.getStringExtra("birthDay");
        birthMonth = intent.getStringExtra("birthMonth");
        birthYear = intent.getStringExtra("birthYear");


    }
}
