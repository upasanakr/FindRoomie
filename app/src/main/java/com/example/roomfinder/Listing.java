package com.example.roomfinder;

public class Listing {
    public int listing_id;
    public String apartment_name;
    public String address;
    public int area;
    public String description;
    public String photos;
    public int no_of_bedrooms;
    public int no_of_bathrooms;
    public String accommodation_type;
    public int no_of_people_sharing;
    public int rent;
    public String available_from;
    public String lease_duration;
    public String smoking_preference;
    public String drinking_preference;
    public String has_smoker;
    public String has_drinker;
    public String city;
    public String food_preference;

    public boolean detailsVisible;

    public Listing(int listing_id, String apartment_name, String address, int area, String description, String photos,
                   int no_of_bedrooms, int no_of_bathrooms, String accommodation_type, int no_of_people_sharing,
                   int rent, String available_from, String lease_duration, String smoking_preference, String drinking_preference,
                   String has_smoker, String has_drinker, String city, String food_preference) {
        this.listing_id = listing_id;
        this.apartment_name = apartment_name;
        this.address = address;
        this.area = area;
        this.description = description;
        this.photos = photos;
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
        this.detailsVisible = false;
    }

    // Constructor (optional), getters, and setters
}
