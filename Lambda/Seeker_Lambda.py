import pymysql
import json
from decimal import Decimal
import traceback

# Database connection details
host = 'cmpe277-roommate-finder-dont-hack.c9uo22yuiopg.us-east-1.rds.amazonaws.com'
user = 'admin'
password = 'WB6M1j6ljMVmH0qfvFEg'
database = 'findRoomie'

# Custom serializer function for Decimal objects


def decimal_serializer(obj):
    if isinstance(obj, Decimal):
        return float(obj)
    return None


def lambda_handler(event, context):
    # Establish database connection
    connection = pymysql.connect(host=host,
                                 user=user,
                                 password=password,
                                 database=database)

    try:
        if event['httpMethod'] == 'POST':
            # Parse the request body as JSON
            request_body = json.loads(event['body'])

            # Extract data from the request body
            user_id = request_body['data']['user_id']
            # print(request_body)
            accommodation_type = request_body['data']['accommodation_type']
            no_of_people_sharing = request_body['data']['no_of_people_sharing']
            veg_status = request_body['data']['veg_status']
            start_date = request_body['data']['start_date']
            max_budget = request_body['data']['max_budget']
            landmark_preferences = request_body['data']['landmark_preferences']
            no_of_bedrooms = request_body['data']['no_of_bedrooms']
            no_of_bathrooms = request_body['data']['no_of_bathrooms']
            apartment_preference = request_body['data']['apartment_preference']
            city_preference = request_body['data']['city_preference']
            smoking_preference = request_body['data']['smoking_preference']
            drinking_preference = request_body['data']['drinking_preference']
            smoker = request_body['data']['smoker']
            drinker = request_body['data']['drinker']
            # Insert data into the Seeker_Preferences table
            with connection.cursor() as cursor:
                sql_query = """INSERT INTO Seeker_Preferences (user_id, accommodation_type, no_of_people_sharing, veg_status, start_date,
                                                              max_budget, landmark_preferences, no_of_bedrooms, no_of_bathrooms,
                                                              apartment_preference, city_preference, smoking_preference, drinking_preference, smoker, drinker)
                               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
                cursor.execute(sql_query, (user_id, accommodation_type, no_of_people_sharing, veg_status, start_date,
                                           max_budget, landmark_preferences, no_of_bedrooms, no_of_bathrooms,
                                           apartment_preference, city_preference, smoking_preference, drinking_preference, smoker, drinker))
                connection.commit()

            return {
                'statusCode': 200,
                'body': json.dumps({'message': 'Data inserted successfully'})
            }
        else:
            return {
                'statusCode': 405,
                'body': json.dumps({'message': 'Method Not Allowed'})
            }
    except Exception as e:
        traceback.print_exc()
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }
    finally:
        # Close database connection
        connection.close()
