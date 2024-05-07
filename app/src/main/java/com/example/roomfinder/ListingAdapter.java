package com.example.roomfinder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private List<Listing> listings;

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

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = listings.get(position);
        Context context = holder.itemView.getContext();

        // Set the basic information
        holder.tvApartmentName.setText(listing.apartment_name);
        holder.tvAddress.setText(listing.address);
        holder.tvRent.setText(context.getString(R.string.rent_pattern, listing.rent));

        // Set the additional details
        holder.tvDescription.setText(listing.description);
        holder.tvArea.setText(context.getString(R.string.area_pattern, listing.area));
        holder.tvNoOfBedrooms.setText(context.getString(R.string.bedrooms_pattern, listing.no_of_bedrooms));
        holder.tvNoOfBathrooms.setText(context.getString(R.string.bathrooms_pattern, listing.no_of_bathrooms));
        holder.tvAccommodationType.setText(context.getString(R.string.accommodation_pattern, listing.accommodation_type));
        holder.tvNoOfPeopleSharing.setText(context.getString(R.string.people_sharing_pattern, listing.no_of_people_sharing));
        holder.tvAvailableFrom.setText(context.getString(R.string.available_from_pattern, listing.available_from));
        holder.tvLeaseDuration.setText(context.getString(R.string.lease_duration_pattern, listing.lease_duration));
        holder.tvSmokingPreference.setText(context.getString(R.string.smoking_preference_pattern, listing.smoking_preference));
        holder.tvDrinkingPreference.setText(context.getString(R.string.drinking_preference_pattern, listing.drinking_preference));
        holder.tvHasSmoker.setText(context.getString(R.string.has_smoker_pattern, listing.has_smoker));
        holder.tvHasDrinker.setText(context.getString(R.id.tv_has_drinker));
        holder.tvCity.setText(context.getString(R.string.city_pattern, listing.city));
        holder.tvFoodPreference.setText(context.getString(R.string.food_preference_pattern, listing.food_preference));

        // Show listing info dialog when "Accept" button is clicked
        holder.btnAccept.setOnClickListener(v -> showListerInfo(holder.itemView.getContext(), listing));

        holder.btnDecline.setOnClickListener(v -> {
            removeListing(holder.getAdapterPosition());
            Toast.makeText(holder.itemView.getContext(), "Declined", Toast.LENGTH_SHORT).show();
        });

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
    @SuppressLint("SetTextI18n")
    private void showListerInfo(Context context, Listing listing) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_listing_details, null);
        TextView tvListerName = dialogView.findViewById(R.id.tv_lister_name);
        TextView tvListerEmail = dialogView.findViewById(R.id.tv_lister_email);
        TextView tvListerContact = dialogView.findViewById(R.id.tv_lister_contact);
        Button btnClose = dialogView.findViewById(R.id.btn_close);

        // Set the details for the lister
//        tvListerName.setText("Lister Name: " + listing.getListerName());
//        tvListerEmail.setText("Lister Email: " + listing.getListerEmail());
//        tvListerContact.setText("Lister Contact: " + listing.getListerContact());

        tvListerName.setText("Lister Name: " + "Sample Name");
        tvListerEmail.setText("Lister Email: " + "sample@example.com");
        tvListerContact.setText("Lister Contact: " + "123-456-7890");

        // Create and show the dialog
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set click listener for close button
        btnClose.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    // ViewHolder class
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvApartmentName, tvAddress, tvDescription, tvRent, tvAvailableFrom,
                tvArea, tvNoOfBedrooms, tvNoOfBathrooms, tvAccommodationType,
                tvNoOfPeopleSharing, tvLeaseDuration, tvSmokingPreference,
                tvDrinkingPreference, tvHasSmoker, tvHasDrinker, tvCity, tvFoodPreference;

        View layoutDetails;
        Button btnAccept, btnDecline;

        ViewHolder(View itemView) {
            super(itemView);
            tvApartmentName = itemView.findViewById(R.id.tv_apartment_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvRent = itemView.findViewById(R.id.tv_rent);
            tvAvailableFrom = itemView.findViewById(R.id.tv_available_from);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvNoOfBedrooms = itemView.findViewById(R.id.tv_no_of_bedrooms);
            tvNoOfBathrooms = itemView.findViewById(R.id.tv_no_of_bathrooms);
            tvAccommodationType = itemView.findViewById(R.id.tv_accommodation_type);
            tvNoOfPeopleSharing = itemView.findViewById(R.id.tv_no_of_people_sharing);
            tvLeaseDuration = itemView.findViewById(R.id.tv_lease_duration);
            tvSmokingPreference = itemView.findViewById(R.id.tv_smoking_preference);
            tvDrinkingPreference = itemView.findViewById(R.id.tv_drinking_preference);
            tvHasSmoker = itemView.findViewById(R.id.tv_has_smoker);
            tvHasDrinker = itemView.findViewById(R.id.tv_has_drinker);
            tvCity = itemView.findViewById(R.id.tv_city);
            tvFoodPreference = itemView.findViewById(R.id.tv_food_preference);
            layoutDetails = itemView.findViewById(R.id.layout_details);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }
}
