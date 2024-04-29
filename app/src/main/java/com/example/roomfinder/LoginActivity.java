package com.example.roomfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                DatabaseHelper databaseHelper = new DatabaseHelper(LoginActivity.this);
                String userType = databaseHelper.getUserType(email, password);

                if (userType != null) {
                    // Navigate based on the user type
                    Intent intent;
                    if ("seeker".equals(userType)) {
                        intent = new Intent(LoginActivity.this, SeekerHomePageActivity.class);
                    } else if ("lister".equals(userType)) {
                        intent = new Intent(LoginActivity.this, ListerHomePageActivity.class);
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid user type", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                    finish(); // Close the login activity
                } else {

                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
