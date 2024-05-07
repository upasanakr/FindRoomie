package com.example.roomfinder;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SeekerHomePageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ListingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_home_page);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Listing> listings = new ArrayList<>();

        listings.add(new Listing(1, "Maple Apartments", "123 Maple Street", 1200, "Spacious apartment with amenities.", "apt1.jpg", 3, 2, "sharing", 2, 1500, "2024-06-01", "12 months", "yes", "any", "no", "yes", "San Francisco", "veg"));
        listings.add(new Listing(2, "Oak Residences", "456 Oak Avenue", 900, "Cozy apartment with modern amenities.", "apt2.jpg", 2, 1, "private", 1, 1200, "2024-05-15", "6 months", "no", "yes", "yes", "no", "San Jose", "non-veg"));
        listings.add(new Listing(3, "Pine Terrace", "789 Pine Lane", 1400, "Luxury apartment with scenic views.", "apt3.jpg", 4, 3, "sharing", 3, 2000, "2024-07-01", "12 months", "yes", "yes", "no", "yes", "Los Angeles", "veg"));
        listings.add(new Listing(4, "Cedar Grove", "321 Cedar Street", 1100, "Affordable apartment with great location.", "apt4.jpg", 2, 1, "private", 1, 1000, "2024-04-20", "9 months", "no", "any", "yes", "no", "San Diego", "non-veg"));
        listings.add(new Listing(5, "Birch Towers", "654 Birch Boulevard", 1300, "Spacious apartment with garden view.", "apt5.jpg", 3, 2, "sharing", 2, 1800, "2024-05-01", "12 months", "yes", "no", "yes", "yes", "Seattle", "veg"));

        adapter = new ListingAdapter(listings);
        recyclerView.setAdapter(adapter);
    }
}
