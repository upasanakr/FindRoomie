package com.example.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        executorService = Executors.newSingleThreadExecutor();

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (!validateForm(email, password)) {
                return;
            }

            // Submit a task to call the login API
            Future<String> loginFuture = executorService.submit(() -> callLoginApi(email, password));

            try {
                // Fetch the response using Future
                String response = loginFuture.get();

                if (response != null) {
                    // Process the response
                    JSONObject jsonResponse = new JSONObject(response);

                    // Check if the JSON object has a 'user' key
                    if (jsonResponse.has("user")) {
                        // Get the 'user' object
                        JSONObject userObject = jsonResponse.getJSONObject("user");

                        // Check if the 'user' object has a 'user_type' key
                        if (userObject.has("user_type")) {
                            String userType = userObject.getString("user_type");
                            navigateToHomePage(userType);
                        } else {
                            Toast.makeText(LoginActivity.this, "User type not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException | JSONException e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean validateForm(String email, String password) {
        if (email.isEmpty()) {
            etUsername.setError("Email cannot be empty");
            etUsername.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password cannot be empty");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    // Method to call the login API
    private String callLoginApi(String email, String password) throws IOException {
        // Construct the login API URL
        String loginUrl = "https://p9r6bc5kf9.execute-api.us-east-1.amazonaws.com/api/login?email=" + email + "&passwordHash=" + password;

        // Create an HTTP connection
        URL url = new URL(loginUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // Set up the connection properties
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);

        // Get the response from the server
        InputStream inputStream = urlConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        // Close the connections
        bufferedReader.close();
        inputStream.close();
        urlConnection.disconnect();

        // Return the response
        return stringBuilder.toString();
    }

    private void navigateToHomePage(String userType) {
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
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
