package com.example.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeekerHomePageActivity extends AppCompatActivity {

    private static final String TAG = "SeekerHomePageActivity";

    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private ExecutorService executorService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int userId = getIntent().getIntExtra("user_id", -1);
        String apiUrl = "http://54.175.51.201:8080/" + userId + "/recommend";

        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        fetchListings(apiUrl);
    }

    private void fetchListings(String apiUrl) {
        executorService.execute(() -> {
            List<Listing> listings = new ArrayList<>();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    Log.d(TAG, "API Response: " + response.toString()); // Log the API response

                    JSONArray jsonArray = new JSONArray(response.toString());
                    Log.d(TAG, "Number of listings received: " + jsonArray.length()); // Log the number of listings received

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Listing listing = new Listing(
                                jsonObject.has("listing_id") ? jsonObject.optInt("listing_id") : null,
                                jsonObject.optString("apartment_name", null),
                                jsonObject.optString("address", null),
                                jsonObject.has("area") ? jsonObject.optInt("area") : null,
                                jsonObject.optString("description", null),
                                jsonObject.has("no_of_bedrooms") ? jsonObject.optInt("no_of_bedrooms") : null,
                                jsonObject.has("no_of_bathrooms") ? jsonObject.optInt("no_of_bathrooms") : null,
                                jsonObject.optString("accommodation_type", null),
                                jsonObject.has("no_of_people_sharing") ? jsonObject.optInt("no_of_people_sharing") : null,
                                jsonObject.has("rent") ? jsonObject.optInt("rent") : null,
                                jsonObject.optString("available_from", null),
                                jsonObject.optString("lease_duration", null),
                                jsonObject.optString("smoking_preference", null),
                                jsonObject.optString("drinking_preference", null),
                                jsonObject.optString("has_smoker", null),
                                jsonObject.optString("has_drinker", null),
                                jsonObject.optString("city", null),
                                jsonObject.optString("food_preference", null),
                                jsonObject.optString("landmarks", null),
                                jsonObject.has("match_id") ? jsonObject.optInt("match_id") : null,
                                jsonObject.optString("status", null),
                                jsonObject.has("user_id") ? jsonObject.optInt("user_id") : null,
                                jsonObject.optString("veg_status", null)
                        );
                        listings.add(listing);
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error during API call: " + e.getMessage()); // Log the error message
            }
            handler.post(() -> updateUI(listings));
        });
    }

    private void updateUI(List<Listing> listings) {
        adapter = new ListingAdapter(listings);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Intent intent = new Intent(SeekerHomePageActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

