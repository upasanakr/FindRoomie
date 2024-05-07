package com.example.roomfinder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListerHomePageActivity extends AppCompatActivity {

    private static final String TAG = "ListerHomePageActivity";
    private RecyclerView recyclerView;
    private SeekerAdapter adapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lister_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int userId = getIntent().getIntExtra("user_id", -1);
        String apiUrl = "http://54.175.51.201:8080/" + userId + "/listMatches";
        fetchSeekers(apiUrl);
    }

    private void fetchSeekers(String apiUrl) {
        executorService.execute(() -> {
            try {
                Request request = new Request.Builder().url(apiUrl).build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                List<Seeker> seekers = gson.fromJson(responseBody, new TypeToken<List<Seeker>>(){}.getType());

                handler.post(() -> updateUI(seekers));
            } catch (IOException e) {
                Log.e(TAG, "Error during API call: " + e.getMessage());
            }
        });
    }

    private void updateUI(List<Seeker> seekers) {
        adapter = new SeekerAdapter(seekers);
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
            // Handle logout logic
            Intent intent = new Intent(ListerHomePageActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class Seeker {
        String email;
        int match_id;
        String name;
        String phone_number;
    }
}
