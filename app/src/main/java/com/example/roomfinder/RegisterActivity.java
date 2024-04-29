package com.example.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private Spinner spinnerUserType;
    private EditText editTextPassword;
    private Button buttonRegister;
    private DatabaseHelper databaseHelper;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z ]+$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(adapter);

        databaseHelper = new DatabaseHelper(this);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String phoneNumber = editTextPhoneNumber.getText().toString();
                String userType = spinnerUserType.getSelectedItem().toString();
                String password = editTextPassword.getText().toString();

                if (!validateForm(name, email, phoneNumber, password)) {
                    return;
                }
//                String passwordHash = hashPassword(password);
                databaseHelper.addUser(name, email, phoneNumber, userType, password);
                Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                navigateToHomePage(userType);
            }
        });
    }

    private void navigateToHomePage(String userType) {
        Intent intent = null;

        if ("seeker".equals(userType)) {
            intent = new Intent(RegisterActivity.this, SeekerPreferenceActivity.class);
        } else if ("lister".equals(userType)) {
            intent = new Intent(RegisterActivity.this, ListerPreferenceActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        } else {

            Toast.makeText(this, "Error: User type not recognized.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateForm(String name, String email, String phone, String password) {
        if (TextUtils.isEmpty(name) || !NAME_PATTERN.matcher(name).matches()) {
            Toast.makeText(this, "Name should only contain letters and spaces.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            Toast.makeText(this, "Phone number should be 10 digits.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            Toast.makeText(this, "Password should be at least 8 characters long.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
