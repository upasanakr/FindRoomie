package com.example.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roomfinder.ListerPreferenceActivity;
import com.example.roomfinder.R;
import com.example.roomfinder.SeekerPreferenceActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private Spinner spinnerUserType;
    private EditText editTextPassword;
    private Button buttonRegister;

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

        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String phoneNumber = editTextPhoneNumber.getText().toString();
            String userType = spinnerUserType.getSelectedItem().toString().equals("Looking for a Room") ? "seeker" : "lister";
            String password = editTextPassword.getText().toString();

            if (!validateForm(name, email, phoneNumber, password)) {
                return;
            }

            JSONObject postData = new JSONObject();
            try {
                postData.put("name", name);
                postData.put("email", email);
                postData.put("phoneNumber", phoneNumber);
                postData.put("userType", userType);
                postData.put("passwordHash", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Thread(() -> {
                try {
                    String response = sendPostRequest("https://p9r6bc5kf9.execute-api.us-east-1.amazonaws.com/api/register", postData.toString());
                    JSONObject responseObject = new JSONObject(response);
                    String userId = responseObject.optString("userId");  // Assuming the ID is returned as "userId"
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        navigateToHomePage(userType, userId);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Error registering user", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
    }

    private String sendPostRequest(String url, String postData) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(postData);
            wr.flush();
        }

        int responseCode = con.getResponseCode();
        InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK) ? con.getInputStream() : con.getErrorStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Log full response details
        Log.d("HTTP Status Code", String.valueOf(responseCode));
        Log.d("HTTP Headers", con.getHeaderFields().toString());
        Log.d("HTTP Response Body", response.toString());

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return response.toString();
        } else {
            throw new IOException("HTTP Error Response: " + responseCode + " - " + response.toString());
        }
    }


    private void navigateToHomePage(String userType, String userId) {
        Intent intent = null;

        if ("seeker".equals(userType)) {
            intent = new Intent(RegisterActivity.this, SeekerPreferenceActivity.class);
        } else if ("lister".equals(userType)) {
            intent = new Intent(RegisterActivity.this, ListerPreferenceActivity.class);
        }

        if (intent != null) {
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error: User type not recognized.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateForm(String name, String email, String phone, String password) {
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name cannot be empty");
            editTextName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email address");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            editTextPhoneNumber.setError("Phone number should be 10 digits");
            editTextPhoneNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            editTextPassword.setError("Password should be at least 8 characters long");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }
}
