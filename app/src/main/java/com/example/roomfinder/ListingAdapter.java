package com.example.roomfinder;

import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private final List<Listing> listings;
    private final String type;
    private final Runnable onSuccess;



    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public ListingAdapter(List<Listing> listings, String type, Runnable onSuccess) {
        this.listings = listings;
        this.type = type;
        this.onSuccess = onSuccess;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = listings.get(position);

        // Set the basic information
        holder.tvApartmentName.setText(listing.apartment_name);
        holder.tvAddress.setText(listing.address);
        holder.tvCity.setText(holder.itemView.getContext().getString(R.string.city_pattern, listing.city));
        holder.tvRent.setText(holder.itemView.getContext().getString(R.string.rent_pattern, listing.rent));

        String description = (listing.description != null && !listing.description.equals("NULL"))
                ? listing.description
                : "Not Available";
        String landmarks = (listing.landmarks != null && !listing.landmarks.equals("NULL"))
                ? listing.landmarks
                : "Not Available";

        // Set the additional details
        holder.tvDescription.setText(holder.itemView.getContext().getString(R.string.description, description));
        holder.tvLandmarks.setText(holder.itemView.getContext().getString(R.string.landmarks, landmarks));

        holder.tvArea.setText(holder.itemView.getContext().getString(R.string.area_pattern, listing.area));
        holder.tvNoOfBedrooms.setText(holder.itemView.getContext().getString(R.string.bedrooms_pattern, listing.no_of_bedrooms));
        holder.tvNoOfBathrooms.setText(holder.itemView.getContext().getString(R.string.bathrooms_pattern, listing.no_of_bathrooms));
        holder.tvAccommodationType.setText(holder.itemView.getContext().getString(R.string.accommodation_pattern, listing.accommodation_type));
        holder.tvNoOfPeopleSharing.setText(holder.itemView.getContext().getString(R.string.people_sharing_pattern, listing.no_of_people_sharing));
        holder.tvAvailableFrom.setText(holder.itemView.getContext().getString(R.string.available_from_pattern, listing.available_from));
        holder.tvLeaseDuration.setText(holder.itemView.getContext().getString(R.string.lease_duration_pattern, listing.lease_duration));
        holder.tvHasSmoker.setText(holder.itemView.getContext().getString(R.string.has_smoker_pattern, listing.has_smoker));
        holder.tvHasDrinker.setText(holder.itemView.getContext().getString(R.string.has_drinker_pattern, listing.has_drinker));
        holder.tvFoodPreference.setText(holder.itemView.getContext().getString(R.string.food_preference_pattern, listing.veg_status));

        if ("matched apartments".equals(type)) {
            holder.btnAccept.setText("View Lister Details");
            holder.btnAccept.setOnClickListener(v -> updateStatus(
                    holder.itemView.getContext(),
                    listing,
                    "accepted",
                    position,
                    true,
                    () -> {}
            ));
        } else {
            holder.btnAccept.setText("Accept");
            holder.btnAccept.setOnClickListener(v -> updateStatus(
                    holder.itemView.getContext(),
                    listing,
                    "accepted",
                    position,
                    false,
                    () -> {
                        ((SeekerHomePageActivity) holder.itemView.getContext()).moveToMatched(listing, position);
                    }
            ));
        }
        holder.btnDecline.setOnClickListener(v -> updateStatus(
                holder.itemView.getContext(),
                listing,
                "declined",
                position,
                false,
                () -> removeListing(position)
        ));

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

    void removeListing(int position) {
        listings.remove(position);
        notifyItemRemoved(position);
    }

    // Method to show dialog with listing details
    private void updateStatus(Context context, Listing listing, String status, int position, boolean showInfo, Runnable onSuccess) {
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
                    handler.post(() -> {
                        if (showInfo) {
                            showListerInfo(context, listerInfo);
                        } else {
                            listing.status = "accepted";
                            onSuccess.run();
                            notifyDataSetChanged();
                            Toast.makeText(context, "Apartment added to your matched apartment list", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Handle rejected case
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    String message = jsonResponse.get("message").getAsString();
                    handler.post(() -> {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        onSuccess.run();
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

        if (listerInfo == null) {
            Toast.makeText(context, "Lister info not available", Toast.LENGTH_SHORT).show();
            return;
        }
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
        TextView tvApartmentName;
        TextView tvAddress;
        TextView tvDescription;
        TextView tvRent;
        TextView tvAvailableFrom;
        TextView tvArea;
        TextView tvNoOfBedrooms;
        TextView tvNoOfBathrooms;
        TextView tvAccommodationType;
        TextView tvNoOfPeopleSharing;
        TextView tvLeaseDuration;
        TextView tvHasSmoker;
        TextView tvHasDrinker;
        TextView tvCity;
        TextView tvFoodPreference;
        TextView tvLandmarks;

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


    public void addListing(Listing listing) {
        listings.add(listing);
        notifyItemInserted(listings.size() - 1);
    }


}
