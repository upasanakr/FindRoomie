package com.example.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ListerPreferenceActivity extends AppCompatActivity {

    private Button buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lister_preference);

        buttonContinue = findViewById(R.id.buttonContinueToListerHome);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start ListerHomePageActivity
                Intent intent = new Intent(ListerPreferenceActivity.this, ListerHomePageActivity.class);
                startActivity(intent);
            }
        });
    }
}
