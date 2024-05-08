package com.example.roomfinder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SeekerPreferenceActivity extends AppCompatActivity {

    private Button buttonGoToSeekerHome;

    private LinearLayout chatContainer;
    private EditText userInput;
    private Queue<SeekerPreferenceActivity.Question> questionQueue;
    private Map<String, String> responses;
    private Map<String, List<String>> optionsList = new HashMap<>();

    private String classifiedResult = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeker_preference);
        String userId = getIntent().getStringExtra("USER_ID");
        Log.e("Lambda Function", userId);

        buttonGoToSeekerHome = findViewById(R.id.button);

        buttonGoToSeekerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SeekerPreferenceActivity.this, SeekerHomePageActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });
        chatContainer = findViewById(R.id.chatContainer);


        questionQueue = new LinkedList<>();
        responses = new HashMap<>();

        // Add questions to the queue
        questionQueue.offer(new SeekerPreferenceActivity.Question("Accommodation type:", "accommodation_type", false, new String[]{"Sharing", "Single"})); // Button options
        questionQueue.offer(new SeekerPreferenceActivity.Question("If sharing, how many people are sharing the apartment?", "no_of_people_sharing", true)); // Text input
        questionQueue.offer(new SeekerPreferenceActivity.Question("Any Vegetarian/NonVeg Preference:", "veg_status", false, new String[]{"Vegetarian", "Non-Vegetarian", "Any"})); // Button options
        questionQueue.offer(new SeekerPreferenceActivity.Question("When will the lease start from (YYYY-MM-DD):", "start_date", true)); // Text input
        questionQueue.offer(new SeekerPreferenceActivity.Question("Maximum budget per month (in USD):", "max_budget", true)); // Text input
        questionQueue.offer(new SeekerPreferenceActivity.Question("Landmark preferences:", "landmark_preferences", true)); // Text input
        optionsList.put("Landmark preferences:", new ArrayList<>(Arrays.asList("near to school", "near to university", "near to bus stop", "near to grocery store")));// Text input
        questionQueue.offer(new SeekerPreferenceActivity.Question("How many bedrooms are you looking for?", "no_of_bedrooms", true)); // Text input
        questionQueue.offer(new SeekerPreferenceActivity.Question("How many bathrooms are you looking for?", "no_of_bathrooms", true));
        questionQueue.offer(new SeekerPreferenceActivity.Question("Do you mind sharing the room with someone who smokes?", "smoking_preference", false, new String[]{"Yes", "No", "Any"})); // Button options
        questionQueue.offer(new SeekerPreferenceActivity.Question("Do you mind sharing the room with someone who drinks?", "drinking_preference", false, new String[]{"Yes", "No", "Any"})); // Button options
        questionQueue.offer(new SeekerPreferenceActivity.Question("Do you smoke?", "smoker", false, new String[]{"Yes", "No"})); // Button options
        questionQueue.offer(new SeekerPreferenceActivity.Question("Do you drink?", "drinker", false, new String[]{"Yes", "No"})); // Button options
        questionQueue.offer(new SeekerPreferenceActivity.Question("Any specific apartment preferences?", "apartment_preference", true)); // Text input
        questionQueue.offer(new SeekerPreferenceActivity.Question("City preference:", "city_preference", true)); // Text input
        askNextQuestion();
    }

    private void askNextQuestion() {
        if (!questionQueue.isEmpty()) {
            Question question = questionQueue.peek();

            askQuestion(question);
        } else {
            for (Map.Entry<String, String> entry : responses.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            //add to rds
            sendResponsesToLambda();
        }
    }

    private void askQuestion(Question question) {
//        addMessage("System", "Rewrite the following: " + question.getQuestion());
        new OpenAIRequestTask().execute(question.getQuestion());
    }

    private void addButton(String option, Question question) {
        Button button = new Button(this);
        button.setText(option);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response = ((Button) v).getText().toString();
                sendMessage(response);
                responses.put(question.getOrgQuestion(), response);
            }
        });
        chatContainer.addView(button);
    }
    private void addTextInput(Question question) {
        LinearLayout inputLayout = new LinearLayout(this);
        inputLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set layout parameters for the text input field
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f);
        editText.setLayoutParams(editTextParams);
        inputLayout.addView(editText);

        // Set layout parameters for the submit button
        Button submitButton = new Button(this);
        submitButton.setText("Submit");
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f);
        submitButton.setLayoutParams(buttonParams);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response = editText.getText().toString();
                if(optionsList.containsKey(question.getQuestion())){
                    List<String> options = optionsList.get(question.getQuestion());
                    String prompt = "Classify the input that I give that matches the list of classes: "+String.join(",", options)+". Just provide the closest matching one class name in response. nothing else. If it does not match any of the classes provided then return the same sentence back."+" Sentence: "+response;
                    Log.e("DEBUGV", prompt);
                    //Call GPT with response and classify that into options
                    OpenAIClassifyRequestTask task  = new OpenAIClassifyRequestTask();
                    task.execute(prompt);
                    Log.e("DEBUGB", (String)classifiedResult);
                }
                sendMessage(response);
                // Add to RDS
                responses.put(question.getOrgQuestion(), response); // Store response with question as key
            }
        });
        inputLayout.addView(submitButton);

        chatContainer.addView(inputLayout);
    }
    private void sendMessage(String message) {
//        addMessage("User", message);
//        responses.put(chatContainer.getChildAt(chatContainer.getChildCount() - 1).toString(), message);
        askNextQuestion();
    }

    private void addMessage(String sender, String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        chatContainer.addView(textView);
    }

    private static class Question {
        private String question;
        private boolean textInput;
        private String[] options;

        private String orgQue;

        public Question(String question, String orgQue, boolean textInput) {
            this.question = question;
            this.orgQue = orgQue;
            this.textInput = textInput;
        }

        public Question(String question, String orgQue, boolean textInput, String[] options) {
            this.question = question;
            this.textInput = textInput;
            this.options = options;
            this.orgQue = orgQue;
        }

        public String getQuestion() {
            return question;
        }
        public String getOrgQuestion() {
            return orgQue;
        }
        public boolean isTextInput() {
            return textInput;
        }

        public String[] getOptions() {
            return options;
        }
    }
    private class OpenAIRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String prompt = params[0];
            String apiKey = "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";
            String apiUrl = "https://api.openai.com/v1/completions";
            String result = "";

            try {
                Log.e("DEBUGASHISH", "Till Here3" );
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("model", "gpt-3.5-turbo-instruct");
                jsonObject.put("prompt", "This is a question to person looking for accommodation/room, so rewrite accordingly for better understanding: "+prompt);
                jsonObject.put("max_tokens", 150);

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
                Log.e("DEBUGASHISH", String.valueOf(conn.getResponseCode()));
                // Check if the request was successful
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        result += line + "\n";
                    }

                    br.close();
                } else {
                    // Handle error response
                    Log.e("Error", "Error here " + conn.getResponseCode());
                    result = "Error: " + conn.getResponseMessage();
                }

                // Close the connection
                conn.disconnect();
            } catch (Exception e) {
                Log.e("DEBUGV", "Error here " + e);
                e.printStackTrace();
            }
            return result;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.startsWith("Error:")) {
                // Handle the error case
                System.out.println("Error occurred: " + result);
            } else {
                try {
                    System.out.println("Response from OpenAI: " + result);
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject generatedResponse = choices.getJSONObject(0);
                    String generatedText = generatedResponse.getString("text");
                    addMessage("System", generatedText);
                    Log.e("DEBUGV", String.valueOf(questionQueue.peek().getQuestion()));
                    Log.e("DEBUGV", String.valueOf(questionQueue.peek().isTextInput()));
                    if (questionQueue.peek().isTextInput()) {
                        Question questionTemp = questionQueue.poll();
                        //Check question to send to gpt for filtering
                        addTextInput(questionTemp);
                        // check user input class
                    }else{
                        Log.e("DEBUGV", String.valueOf(questionQueue.peek().getQuestion()));
                        Log.e("DEBUGV", String.valueOf(questionQueue.peek().getOptions()));
                        Question questionTemp = questionQueue.poll();
                        for (String option : questionTemp.getOptions()) {
                            addButton(option, questionTemp);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void sendResponsesToLambda() {
        try {
            // Convert the responses HashMap to JSON
            JSONObject jsonData = new JSONObject();
//            responses.put("photos","Sample.png");
            String userId = getIntent().getStringExtra("USER_ID");
            Log.e("Lambda Function", userId);
            responses.put("user_id",userId);
            if(!responses.containsKey("city_preference")){
                responses.put("city_preference","San Jose");
            }
            JSONObject userResponses = new JSONObject(responses); // Convert responses HashMap to JSONObject

            // Add user responses to the JSON data
            jsonData.put("data", userResponses);
            Log.e("LambdaFunction", "Debugging: " + jsonData);
            // Lambda function URL
            String lambdaUrl = "https://ife9c2w20i.execute-api.us-east-2.amazonaws.com/default/CMPE277_Seeker";

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        URL url = new URL(lambdaUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(15000);
                        conn.setConnectTimeout(15000);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json"); // Set content type
                        conn.setDoInput(true);
                        conn.setDoOutput(true);

                        // Send JSON data
                        OutputStream os = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write(jsonData.toString());
                        writer.flush();
                        writer.close();
                        os.close();

                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            // Success
                            Log.d("LambdaFunction", "Lambda function call successful");
                        } else {
                            // Error handling
                            Log.e("LambdaFunction", "Error response code: " + responseCode);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("LambdaFunction", "Exception: " + e.getMessage());
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("LambdaFunction", "Exception: " + e.getMessage());
        }
    }


    private class OpenAIClassifyRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String prompt = params[0];
            String apiKey = "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";
            String apiUrl = "https://api.openai.com/v1/completions";
            String result = "";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setDoOutput(true);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("model", "gpt-3.5-turbo-instruct");
                jsonObject.put("prompt", prompt);
                jsonObject.put("max_tokens", 150);

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                // Check if the request was successful
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Thread.sleep(3000);
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        result += line + "\n";
                    }
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject generatedResponse = choices.getJSONObject(0);
                    Log.e("DEBUGA", generatedResponse.getString("text"));
                    classifiedResult = generatedResponse.getString("text");
                    Log.e("DEBUGA", classifiedResult);
                    br.close();
                } else {
                    // Handle error response
                    Log.e("Error", "Error here " + conn.getResponseCode());
                    result = "Error: " + conn.getResponseMessage();
                }

                // Close the connection
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.startsWith("Error:")) {
                // Handle the error case
                System.out.println("Error occurred: " + result);
            } else {
                try {
                    //System.out.println("Response from OpenAI: " + result);
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject generatedResponse = choices.getJSONObject(0);
                    //Log.e("DEBUGV", generatedResponse.getString("text"));
                    //classifiedResult = generatedResponse.getString("text");
                    Log.e("DEBUGV", String.valueOf(questionQueue.peek().getQuestion()));
                    Log.e("DEBUGV", String.valueOf(questionQueue.peek().isTextInput()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}