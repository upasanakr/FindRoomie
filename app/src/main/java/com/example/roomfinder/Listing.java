package com.example.roomfinder;

public class Listing {
    public Integer listing_id;
    public String apartment_name;
    public String address;
    public Integer area;
    public String description;
    public Integer no_of_bedrooms;
    public Integer no_of_bathrooms;
    public String accommodation_type;
    public Integer no_of_people_sharing;
    public Integer rent;
    public String available_from;
    public String lease_duration;
    public String smoking_preference;
    public String drinking_preference;
    public String has_smoker;
    public String has_drinker;
    public String city;
    public String food_preference;
    public String landmarks;
    public Integer match_id;
    public String status;
    public Integer user_id;
    public String veg_status;

    public boolean detailsVisible;
    ListingAdapter.ListerInfo listerInfo;

    public Listing(Integer listing_id, String apartment_name, String address, Integer area, String description,
                   Integer no_of_bedrooms, Integer no_of_bathrooms, String accommodation_type, Integer no_of_people_sharing,
                   Integer rent, String available_from, String lease_duration, String smoking_preference,
                   String drinking_preference, String has_smoker, String has_drinker, String city,
                   String food_preference, String landmarks, Integer match_id, String status, Integer user_id,
                   String veg_status) {
        this.listing_id = listing_id;
        this.apartment_name = apartment_name;
        this.address = address;
        this.area = area;
        this.description = description;
        this.no_of_bedrooms = no_of_bedrooms;
        this.no_of_bathrooms = no_of_bathrooms;
        this.accommodation_type = accommodation_type;
        this.no_of_people_sharing = no_of_people_sharing;
        this.rent = rent;
        this.available_from = available_from;
        this.lease_duration = lease_duration;
        this.smoking_preference = smoking_preference;
        this.drinking_preference = drinking_preference;
        this.has_smoker = has_smoker;
        this.has_drinker = has_drinker;
        this.city = city;
        this.food_preference = food_preference;
        this.landmarks = landmarks;
        this.match_id = match_id;
        this.status = status;
        this.user_id = user_id;
        this.veg_status = veg_status;
        this.detailsVisible = false;
    }

    // Constructor (optional), getters, and setters
}
