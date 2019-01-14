package com.example.chronos.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LogInSignUpOptionsActivity extends AppCompatActivity {

    Button logInButton;
    Button signUpButton;
    Button facebookButton;

    public void onSignUpPressed(View view) {
        Intent intent = new Intent(this, Register1stActivity.class);
        startActivity(intent);
    }

    public void onLogInPressed(View view) {
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
    }

    public void onFacebookPressed(View view) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_sign_up_options);

        logInButton = findViewById(R.id.logInButton);
        signUpButton = findViewById(R.id.signUpButton);
        facebookButton = findViewById(R.id.facebookButton);
    }
}
