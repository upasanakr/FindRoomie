import json
from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
import pickle
import mysql.connector
from mysql.connector import Error
from datetime import datetime
from datetime import timedelta
import csv

app = Flask(__name__)

def connect_to_database():
    try:
        connection = mysql.connector.connect(host='xxx',
                                             database='xxx',
                                             user='xxx',
                                             password='xxx')
        return connection
    except Error as e:
        print("Error connecting to database:", e)
        return None

# Load the model
def load_model():
    with open('svd_model.pkl', 'rb') as f:
        model = pickle.load(f)
    return model

# Load listings data
def load_listings():
    listings_df = pd.read_csv('listings.csv')
    return listings_df

# Preprocess the input data
def preprocess_input(data):
    # Replace empty values in 'no_of_people_sharing' column with 0
    data['no_of_people_sharing'] = data.get('no_of_people_sharing', 0)

    # Remove rows with null values in specific columns
    cols_to_replace_seekers = ['landmark_preferences', 'apartment_preference']
    for col in cols_to_replace_seekers:
        data[col] = data.get(col, 'no preference')

    # Convert the input JSON data into a DataFrame
    new_seeker_df = pd.DataFrame([data])

    # One-hot encode categorical columns
    categorical_columns = ['accommodation_type', 'veg_status', 'smoking_preference', 'drinking_preference', 'city_preference']
    for col in categorical_columns:
        new_seeker_df[col] = new_seeker_df[col].fillna('no preference')
    new_seeker_df = pd.get_dummies(new_seeker_df, columns=categorical_columns)

    return new_seeker_df

# Generate top recommendations with an optional offset
def get_top_recommendations(seeker_id, model, listings_df, offset=0):
    listing_scores = {}
    for listing_id in listings_df['listing_id']:
        score = model.predict(uid=seeker_id, iid=listing_id).est
        listing_scores[listing_id] = score
    sorted_listings = sorted(listing_scores.items(), key=lambda x: x[1], reverse=True)
    top_listings = sorted_listings[offset:offset+5]
    return listings_df[listings_df['listing_id'].isin([x[0] for x in top_listings])]


# Insert match into database
def insert_match(seeker_id, listing_id):
    try:
        connection = connect_to_database()
        cursor = connection.cursor()
        current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        insert_query = """INSERT INTO Matches (seeker_id, listing_id, status, generated_datetime, updated_datetime) 
                          VALUES (%s, %s, 'no_action', %s, %s)"""
        match_data = (seeker_id, listing_id, current_time, current_time)
        print('inserting into db: ',match_data)
        cursor.execute(insert_query, match_data)
        connection.commit()
        return True
    except Error as e:
        print("Error inserting match:", e)
        return False
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()

# # Lambda handler function
# @app.route('/recommend', methods=['POST'])
# def recommend():
#     # Load the model and listings data
#     model = load_model()
#     listings_df = load_listings()

#     # Preprocess the input data
#     data = request.json
#     preprocessed_data = preprocess_input(data)

#     # Sample seeker ID (based on the new user ID)
#     seeker_id = data['user_id']

#     # Generate recommendations
#     recommendations = get_top_5_recommendations(seeker_id, model, listings_df)

#     # Insert matches into database
#     for listing_id in recommendations['listing_id']:
#         insert_match(seeker_id, listing_id)

#     # Return the recommendations as a JSON response
#     return jsonify(recommendations.to_dict(orient='records'))

@app.route('/<int:userId>/recommend', methods=['GET'])
def get_recommendations(userId):
    try:
        connection = connect_to_database()
        if connection:
            cursor = connection.cursor(dictionary=True)
            
            # Check if there are any matches for the given userId
            match_query = """
                SELECT m.match_id, m.listing_id, m.status, l.user_id, l.apartment_name, l.address, l.area,
                       l.description, l.no_of_bedrooms, l.no_of_bathrooms, l.accommodation_type,
                       l.no_of_people_sharing, l.rent, l.available_from, l.lease_duration,
                       l.has_smoker, l.has_drinker, l.city, l.veg_status, l.landmarks
                FROM Matches m
                INNER JOIN Listings l ON m.listing_id = l.listing_id
                WHERE m.seeker_id = %s
            """
            cursor.execute(match_query, (userId,))
            matches = cursor.fetchall()
            if matches:
                print("matches found")
                # Check if all matches are not in status rejected
                non_rejected_matches = [match for match in matches if match['status'] != 'declined']
                if non_rejected_matches:
                    print("there are non-rejected matches so not getting any new recommendations")
                    # If there are non-rejected matches, return them as JSON response
                    return jsonify(non_rejected_matches)
                
            
            # If all matches are rejected, count the number of rejected matches
            num_rejected_matches = len(matches)
            print('No. of rejected matches till now', num_rejected_matches)
            
            # Get seeker preferences from the seeker_preferences table
            preferences_query = """
                SELECT * FROM Seeker_Preferences WHERE user_id = %s
            """
            cursor.execute(preferences_query, (userId,))
            seeker_preferences = cursor.fetchone()
            
            print('fetched seeker preference for matching: ', seeker_preferences)
            # Get top scored listings based on seeker preferences
            top_listings = filter_top_listings(seeker_preferences, offset=num_rejected_matches)

            # Get details of the inserted listings for the response
            matches_data = get_listing_details_for_matches(userId)

            print('matches_data fetched length', len(matches_data))
            
            # Return top listings along with seeker preferences
            return jsonify(matches_data)
    except Exception as e:
        print("Error:", e)
        return jsonify({"error": "An error occurred while fetching recommendations."}), 500
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()

def get_listing_details_for_matches(user_id):
    try:
        connection = connect_to_database()
        if connection:
            cursor = connection.cursor(dictionary=True)
            select_query = """
                SELECT m.match_id, m.listing_id, m.status, l.user_id, l.apartment_name, l.address, l.area,
                       l.description, l.no_of_bedrooms, l.no_of_bathrooms, l.accommodation_type,
                       l.no_of_people_sharing, l.rent, l.available_from, l.lease_duration,
                       l.has_smoker, l.has_drinker, l.city, l.veg_status, l.landmarks
                FROM Matches m
                INNER JOIN Listings l ON m.listing_id = l.listing_id
                WHERE m.seeker_id = %s
            """
            cursor.execute(select_query, (user_id,))
            matches_data = cursor.fetchall()
            return matches_data
    except Error as e:
        print("Error retrieving listing details for matches:", e)
        return None
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()

def filter_top_listings(seeker_preferences, offset):
    print('inside top listings filtering method with offset', offset)
    # Convert seeker_preferences to list
    seeker_prefs = [
        seeker_preferences['preference_id'],
        seeker_preferences['user_id'],
        seeker_preferences['accommodation_type'],
        seeker_preferences['no_of_people_sharing'],
        seeker_preferences['veg_status'],
        seeker_preferences['start_date'],
        seeker_preferences['max_budget'],
        seeker_preferences['landmark_preferences'],
        seeker_preferences['no_of_bedrooms'],
        seeker_preferences['no_of_bathrooms'],
        seeker_preferences['apartment_preference'],
        seeker_preferences['city_preference'],
        seeker_preferences['smoking_preference'],
        seeker_preferences['drinking_preference'],
        seeker_preferences['smoker'],
        seeker_preferences['drinker']
    ]

    print('seeker_preferences list', seeker_prefs)
    
    # Load listings from listings CSV
    listings = load_listings_from_csv()
    
    # Filter listings by city and start date
    seeker_city = seeker_prefs[11]
    seeker_start_date = seeker_prefs[5]
    seeker_start_date = datetime.combine(seeker_start_date, datetime.min.time())
    city_listings = [listing for listing in listings if listing[18] == seeker_city and listing[12].date() >= seeker_start_date.date() + timedelta(days=30*2)]

    print('remaining listings after basic filtering', len(city_listings))
    
    # Calculate match score for each filtered listing based on seeker preferences
    scored_listings = []
    for listing in city_listings:
        score = match_score(seeker_prefs, listing)
        scored_listings.append((listing, score))
    
    # Sort listings based on match score
    scored_listings.sort(key=lambda x: x[1], reverse=True)
    
    # Return top listings based on offset
    top_listings = scored_listings[offset:offset+5]
    
    # Insert top listings into the matches table
    insert_top_listings_to_matches(top_listings, seeker_prefs[1])  # user_id
    
    # Return the top scored listings
    return [listing[0] for listing in top_listings]

def load_listings_from_csv():
    print('loading listings')
    listings = []
    with open('listings.csv', newline='') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            listings.append([
                int(row['listing_id']),
                int(row['user_id']),
                row['apartment_name'],
                row['address'],
                int(row['area']),
                row['description'],
                row['photos'],
                int(row['no_of_bedrooms']),
                int(row['no_of_bathrooms']),
                row['accommodation_type'],
                int(row['no_of_people_sharing']) if row['no_of_people_sharing'] else None,
                float(row['rent']),
                datetime.strptime(row['available_from'], '%Y-%m-%d'),
                row['lease_duration'],
                row['smoking_preference'],
                row['drinking_preference'],
                row['has_smoker'],
                row['has_drinker'],
                row['city'],
                row['veg_status'],
                row['landmark']
            ])
    print('no. of listings loaded', len(listings))
    return listings

def match_score(seeker, listing):
    score = 0
    if seeker[6] >= listing[11]:  # Budget
        score += 3
    if seeker[2] == listing[10]:  # Accommodation type
        score += 2
        if seeker[2] == 'single':
            if listing[10] == 'single':
                score += 1
        elif seeker[2] == 'sharing':
            if listing[10] == 'sharing':
                score += 1
                if seeker[3] == listing[11]:  # No. of people sharing
                    score += 1
                elif abs(seeker[3] - listing[11]) == 1:
                    score += 0.5
                elif abs(seeker[3] - listing[11]) == 2:
                    score += 0.25
    if match_pref_with_actual(seeker[12], listing[15]):  # Smoking preference
        score += 2
    if match_pref_with_actual(seeker[13], listing[16]):  # Drinking preference
        score += 2
    if match_pref_with_actual(seeker[4], listing[19]):  # Veg status preference
        score += 2
    if match_pref_with_actual(seeker[7], listing[20], True):  # Landmark preference
        score += 1
    if match_pref_with_actual(seeker[10], listing[5], True):  # Apartment preference
        score += 1
    if seeker[8] == listing[8] and seeker[9] == listing[9]:  # Number of bedrooms/bathrooms
        score += 1
    return score

def insert_top_listings_to_matches(top_listings, user_id):
    try:
        connection = connect_to_database()
        if connection:
            cursor = connection.cursor()
            current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            insert_query = """INSERT INTO Matches (seeker_id, listing_id, status, generated_datetime, updated_datetime) 
                              VALUES (%s, %s, 'no_action', %s, %s)"""
            for listing, _ in top_listings:
                listing_id = listing[0]  # Assuming listing_id is the first element in the listing tuple
                match_data = (user_id, listing_id, current_time, current_time)
                cursor.execute(insert_query, match_data)
            connection.commit()
            print("Top listings inserted into matches table successfully.")
    except Error as e:
        print("Error inserting top listings into matches table:", e)
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()


# Update match status in database
def update_match_status(match_id, status):
    try:
        connection = connect_to_database()
        if connection:
            cursor = connection.cursor()
            current_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            update_query = """UPDATE Matches SET status = %s, updated_datetime = %s WHERE match_id = %s"""
            match_data = (status, current_time, match_id)
            cursor.execute(update_query, match_data)
            connection.commit()
            return True
    except Error as e:
        print("Error updating match status:", e)
        return False
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()

# Get user details from database based on match ID
def get_user_details(match_id):
    try:
        connection = connect_to_database()
        if connection:
            cursor = connection.cursor(dictionary=True)
            select_query = """
                SELECT u.name, u.email, u.phone_number
                FROM Matches m
                INNER JOIN Listings l ON m.listing_id = l.listing_id
                INNER JOIN Users u ON l.user_id = u.user_id
                WHERE m.match_id = %s
            """
            cursor.execute(select_query, (match_id,))
            user_details = cursor.fetchone()
            return user_details
    except Error as e:
        print("Error retrieving user details:", e)
        return None
    finally:
        if connection and connection.is_connected():
            cursor.close()
            connection.close()

# Lambda handler function
@app.route('/<int:matchId>/statusChange', methods=['POST'])
def status_change(matchId):
    # Get status from request body
    status_data = request.json
    if not status_data or 'status' not in status_data:
        return jsonify({"error": "Status not provided in request body."}), 400

    status = status_data['status']

    # Update match status in database
    if update_match_status(matchId, status):
        if status == 'accepted':
            # Get user details of lister from database
            match_details = get_user_details(matchId)
            if match_details:
                match_details['match_id'] = matchId
                return jsonify(match_details)
            else:
                return jsonify({"error": "User details not found for the match."}), 404
        else:
            return jsonify({"message": "Match status updated successfully."})
    else:
        return jsonify({"error": "Failed to update match status."}), 500

from flask import jsonify

@app.route('/<int:userId>/listMatches', methods=['GET'])
def list_matches(userId):
    try:
        connection = connect_to_database()
        if connection:
            cursor = connection.cursor(dictionary=True)
            
            # Get the listing_id for the given userId
            listing_query = """
                SELECT listing_id FROM Listings WHERE user_id = %s
            """
            cursor.execute(listing_query, (userId,))
            listing_result = cursor.fetchone()
            if listing_result:
                listing_id = listing_result['listing_id']
                
                # Get matches with accepted status for the listing_id
                match_query = """
                    SELECT m.match_id, u.name, u.email, u.phone_number 
                    FROM Matches m 
                    INNER JOIN Users u ON m.seeker_id = u.user_id 
                    WHERE m.listing_id = %s AND m.status = 'accepted'
                """
                cursor.execute(match_query, (listing_id,))
                matches = cursor.fetchall()
                
                if matches:
                    return jsonify(matches), 200
                else:
                    return jsonify({"message": "No matches found for the listing."}), 404
            else:
                return jsonify({"message": "No listing found for the user."}), 404
                
    except Exception as e:
        print("Error:", e)
        return jsonify({"message": "Internal Server Error"}), 500
    finally:
        if connection:
            connection.close()

# Function to compare preferences
def match_pref_with_actual(pref, actual, is_keyword=False):
    if is_keyword:
        return any(keyword in actual.lower() for keyword in pref.lower().split())
    else:
        return pref == 'any' or actual == 'any' or pref == actual



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
