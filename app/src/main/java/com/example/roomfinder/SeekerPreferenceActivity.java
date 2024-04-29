package com.example.roomfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SeekerPreferenceActivity extends AppCompatActivity {

    private Button buttonGoToSeekerHome;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_preference);

        buttonGoToSeekerHome = findViewById(R.id.buttonGoToSeekerHome);

        buttonGoToSeekerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SeekerPreferenceActivity.this, SeekerHomePageActivity.class);
                startActivity(intent);
            }
        });
    }
}
