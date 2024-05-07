package com.example.roomfinder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.*;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private List<Listing> listings;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();


    public ListingAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"ResourceType", "StringFormatInvalid"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = listings.get(position);
        Context context = holder.itemView.getContext();

        // Set the basic information
        holder.tvApartmentName.setText(listing.apartment_name);
        holder.tvAddress.setText(listing.address);
        holder.tvRent.setText(context.getString(R.string.rent_pattern, listing.rent));

        // Set the additional details
        holder.tvDescription.setText(context.getString(R.string.description, listing.description));
        holder.tvLandmarks.setText(context.getString(R.string.landmarks, listing.landmarks));
        holder.tvArea.setText(context.getString(R.string.area_pattern, listing.area));
        holder.tvNoOfBedrooms.setText(context.getString(R.string.bedrooms_pattern, listing.no_of_bedrooms));
        holder.tvNoOfBathrooms.setText(context.getString(R.string.bathrooms_pattern, listing.no_of_bathrooms));
        holder.tvAccommodationType.setText(context.getString(R.string.accommodation_pattern, listing.accommodation_type));
        holder.tvNoOfPeopleSharing.setText(context.getString(R.string.people_sharing_pattern, listing.no_of_people_sharing));
        holder.tvAvailableFrom.setText(context.getString(R.string.available_from_pattern, listing.available_from));
        holder.tvLeaseDuration.setText(context.getString(R.string.lease_duration_pattern, listing.lease_duration));
//        holder.tvSmokingPreference.setText(context.getString(R.string.smoking_preference_pattern, listing.smoking_preference));
//        holder.tvDrinkingPreference.setText(context.getString(R.string.drinking_preference_pattern, listing.drinking_preference));
        holder.tvHasSmoker.setText(context.getString(R.string.has_smoker_pattern, listing.has_smoker));
        holder.tvHasDrinker.setText(context.getString(R.string.has_drinker_pattern, listing.has_drinker));
        holder.tvCity.setText(context.getString(R.string.city_pattern, listing.city));
        holder.tvFoodPreference.setText(context.getString(R.string.food_preference_pattern, listing.veg_status));

        // Show listing info dialog when "Accept" button is clicked
        // Show listing info dialog when "Accept" button is clicked
        holder.btnAccept.setOnClickListener(v -> updateStatus(holder.itemView.getContext(), listing, "accepted",position));
        holder.btnDecline.setOnClickListener(v -> updateStatus(holder.itemView.getContext(), listing, "declined",position));

        holder.itemView.setOnClickListener(v -> {
            listing.detailsVisible = !listing.detailsVisible;
            holder.layoutDetails.setVisibility(listing.detailsVisible ? View.VISIBLE : View.GONE);
        });

        holder.layoutDetails.setVisibility(listing.detailsVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    private void removeListing(int position) {
        listings.remove(position);
        notifyItemRemoved(position);
    }

    // Method to show dialog with listing details
    private void updateStatus(Context context, Listing listing, String status,int position) {
        executorService.execute(() -> {
            try {
                // Create the JSON object
                JsonObject statusUpdate = new JsonObject();
                statusUpdate.addProperty("status", status);

                // Convert the JSON object to a string
                String json = gson.toJson(statusUpdate);

                // Make the POST request
                RequestBody requestBody = RequestBody.create(
                        json,
                        MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("http://54.175.51.201:8080/" + listing.match_id + "/statusChange")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (status.equals("accepted")) {
                    // Parse response and show info dialog
                    ListerInfo listerInfo = gson.fromJson(responseBody, ListerInfo.class);
                    handler.post(() -> showListerInfo(context, listerInfo));
                } else {
                    // Handle rejected case
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    String message = jsonResponse.get("message").getAsString();
                    handler.post(() -> {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                        removeListing(position);
                    });
                }
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    // Method to show dialog with listing details
    @SuppressLint("SetTextI18n")
    private void showListerInfo(Context context, ListerInfo listerInfo) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_listing_details, null);
        TextView tvListerName = dialogView.findViewById(R.id.tv_lister_name);
        TextView tvListerEmail = dialogView.findViewById(R.id.tv_lister_email);
        TextView tvListerContact = dialogView.findViewById(R.id.tv_lister_contact);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        tvListerName.setText("Lister Name: " + listerInfo.name);
        tvListerEmail.setText("Lister Email: " + listerInfo.email);
        tvListerContact.setText("Lister Contact: " + listerInfo.phone_number);

        // Create and show the dialog
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set click listener for close button
        btnClose.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvApartmentName, tvAddress, tvDescription, tvRent, tvAvailableFrom,
                tvArea, tvNoOfBedrooms, tvNoOfBathrooms, tvAccommodationType,
                tvNoOfPeopleSharing, tvLeaseDuration, tvSmokingPreference,
                tvDrinkingPreference, tvHasSmoker, tvHasDrinker, tvCity, tvFoodPreference, tvLandmarks;

        View layoutDetails;
        Button btnAccept, btnDecline;

        ViewHolder(View itemView) {
            super(itemView);
            tvApartmentName = itemView.findViewById(R.id.tv_apartment_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvLandmarks = itemView.findViewById(R.id.tv_landmarks);
            tvRent = itemView.findViewById(R.id.tv_rent);
            tvAvailableFrom = itemView.findViewById(R.id.tv_available_from);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvNoOfBedrooms = itemView.findViewById(R.id.tv_no_of_bedrooms);
            tvNoOfBathrooms = itemView.findViewById(R.id.tv_no_of_bathrooms);
            tvAccommodationType = itemView.findViewById(R.id.tv_accommodation_type);
            tvNoOfPeopleSharing = itemView.findViewById(R.id.tv_no_of_people_sharing);
            tvLeaseDuration = itemView.findViewById(R.id.tv_lease_duration);
//            tvSmokingPreference = itemView.findViewById(R.id.tv_smoking_preference);
//            tvDrinkingPreference = itemView.findViewById(R.id.tv_drinking_preference);
            tvHasSmoker = itemView.findViewById(R.id.tv_has_smoker);
            tvHasDrinker = itemView.findViewById(R.id.tv_has_drinker);
            tvCity = itemView.findViewById(R.id.tv_city);
            tvFoodPreference = itemView.findViewById(R.id.tv_food_preference);
            layoutDetails = itemView.findViewById(R.id.layout_details);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }
    static class ListerInfo {
        String name;
        String email;
        String phone_number;
    }

}
