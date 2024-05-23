import pymysql
import json
import traceback

# Database connection details
host = 'cmpe277-roommate-finder-dont-hack.c9uo22yuiopg.us-east-1.rds.amazonaws.com'
user = 'admin'
password = 'WB6M1j6ljMVmH0qfvFEg'
database = 'findRoomie'
# connection = None


def lambda_handler(event, context):
    try:
        # Extracting the data from the event payload
        body = json.loads(event['body'])

        # Extract the user_id from the parsed body
        # user_id = body['data']['user_id']
        # print(event,user_id)
        data = (
            body['data']['user_id'],
            body['data']['apartment_name'],
            body['data']['address'],
            body['data']['area'],
            body['data']['description'],
            body['data']['photos'],
            body['data']['no_of_bedrooms'],
            body['data']['no_of_bathrooms'],
            body['data']['accommodation_type'],
            body['data']['no_of_people_sharing'],
            body['data']['rent'],
            body['data']['available_from'],
            body['data']['lease_duration'],
            body['data']['smoking_preference'],
            body['data']['drinking_preference'],
            body['data']['has_smoker'],
            body['data']['has_drinker'],
            body['data']['veg_status'],
            body['data']['city'],
            body['data']['landmarks']
        )

        # Establish database connection
        connection = pymysql.connect(host=host,
                                     user=user,
                                     password=password,
                                     database=database)

        if connection:

            # Construct the SQL query to insert the new data
            sql_query = """
                INSERT INTO Listings (user_id, apartment_name, address, area, description, photos, 
                                      no_of_bedrooms, no_of_bathrooms, accommodation_type, 
                                      no_of_people_sharing, rent, available_from, lease_duration, 
                                      smoking_preference, drinking_preference, has_smoker, has_drinker, 
                                      veg_status, city, landmarks)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """

            with connection.cursor() as cursor:
                # Execute the SQL query with the provided data
                cursor.execute(sql_query, data)

                # Commit the transaction
                connection.commit()

            return {
                'statusCode': 200,
                'body': 'Data inserted successfully'
            }
    except Exception as e:
        traceback.print_exc()
        return {
            'statusCode': 500,
            'body': str(e)
        }
    finally:
        # Close database connection
        if connection:
            connection.close()
