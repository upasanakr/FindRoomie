package com.example.roomfinder;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the buttons
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Set click listeners for the buttons
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle registration button click
                // Launch RegisterActivity (implement this activity)
                //Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                //startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle login button click
                // Launch LoginActivity (implement this activity)
                //Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                //startActivity(intent);
            }
        });
    }
}
