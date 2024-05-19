import pickle
import pandas as pd
import json
from flask import Flask, request, jsonify

# Load the model
model_path = '/opt/ml/processing/model/svd_model.pkl'
#model_path = 'svd_model.pkl'
with open(model_path, 'rb') as f:
    model = pickle.load(f)

app = Flask(__name__)

# Load listings data
listings_path = '/opt/ml/processing/model/listings.csv'  # Adjust path as needed
#listings_path = 'listings.csv'
listings_df = pd.read_csv(listings_path)  # Ensure this file is kept updated

@app.route('/ping', methods=['GET'])
def ping():
    # Health check endpoint
    return jsonify({"status": "ok"})

@app.route('/invocations', methods=['POST'])
def predict():
    # Accept JSON input data
    data = json.loads(request.data)

    # Convert the input JSON data into a DataFrame and preprocess
    new_seeker_df = pd.DataFrame([data])
    
    # Replace empty values in 'no_of_people_sharing' column with 0
    new_seeker_df['no_of_people_sharing'] = new_seeker_df['no_of_people_sharing'].fillna(0)

    # Remove rows with null values in specific columns
    cols_to_replace_seekers = ['landmark_preferences', 'apartment_preference']
    new_seeker_df[cols_to_replace_seekers] = new_seeker_df[cols_to_replace_seekers].fillna('no preference')


    # Align with the original DataFrame's columns
    new_seeker_df = new_seeker_df.reindex(columns=seeker_preferences_df.columns)

    # Preprocess: one-hot encoding with accurate column names from seeker_preferences_df
    categorical_columns = ['accommodation_type', 'veg_status', 'smoking_preference', 'drinking_preference', 'city_preference']
    categorical_columns = [col for col in categorical_columns if col in new_seeker_df.columns]

    new_seeker_df = pd.get_dummies(new_seeker_df, columns=categorical_columns)

    # Align with the training DataFrame
    aligned_sample = pd.DataFrame(columns=seeker_preferences_df.columns)
    aligned_sample.update(new_seeker_df)
    aligned_sample = aligned_sample.fillna(0)
    
    # Generate top 5 recommendations
    listing_scores = {}
    for listing_id in listings_df['listing_id']:
        score = model.predict(uid=data['user_id'], iid=listing_id).est
        listing_scores[listing_id] = score
    top_5_listings = sorted(listing_scores.items(), key=lambda x: x[1], reverse=True)[:5]
    recommendations = listings_df[listings_df['listing_id'].isin([x[0] for x in top_5_listings])]

    # Return the recommendations as a JSON response
    return recommendations.to_json(orient='records')

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
