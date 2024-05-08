package com.example.roomfinder;

import android.content.Intent;
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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ListerPreferenceActivity extends AppCompatActivity {

    private Button buttonContinue;

    private LinearLayout chatContainer;
    private EditText userInput;
    private Queue<ListerPreferenceActivity.Question> questionQueue;
    private Map<String, String> responses;
    private Map<String, List<String>> optionsList= new HashMap<>();

    private String classifiedResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lister_preference);
        String userId = getIntent().getStringExtra("USER_ID");
        Log.e("Lambda Function", userId);
        chatContainer = findViewById(R.id.chatContainer);
        questionQueue = new LinkedList<>();
        responses = new HashMap<>();

        // Add questions to the queue
        questionQueue.offer(new ListerPreferenceActivity.Question("What is the name of the apartment?", "apartment_name", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("What is the address of the apartment?", "address", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("What is the area of the apartment in square feet?", "area", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("Please describe the apartment:", "description", true)); // Text input
        optionsList.put("Please describe the apartment:", new ArrayList<>(Arrays.asList("spacious apartment", "cozy room", "modern flat", "elegant house", "apartment with parking", "gated community", "pet-friendly", "gym facility", "pool access")));
//        questionQueue.offer(new Question("Upload photos of the apartment", true)); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("How many bedrooms does the apartment have?", "no_of_bedrooms", true)); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("How many bathrooms does the apartment have?", "no_of_bathrooms", true)); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("Accommodation type:", "accommodation_type", false, new String[]{"Sharing", "Single"})); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("If sharing, how many people are sharing the apartment?", "no_of_people_sharing", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("Rent per month (in USD):", "rent", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("When will the lease start from (YYYY-MM-DD):", "available_from", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("Lease duration (in months):", "lease_duration", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("Do you mind if the person coming smokes?", "smoking_preference",false, new String[]{"Yes", "No", "Any"})); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("Do you mind if the person coming drinks?", "drinking_preference",false, new String[]{"Yes", "No", "Any"})); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("Are there smokers in the apartment?", "has_smoker", false, new String[]{"Yes", "No"})); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("Is drinking allowed in the apartment?", "has_drinker", false, new String[]{"Yes", "No"})); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("Any Vegetarian/NonVeg Preferance:", "veg_status", false, new String[]{"Vegetarian", "Non-Vegetarian", "Any"})); // Button options
        questionQueue.offer(new ListerPreferenceActivity.Question("Which City property is located:", "city", true)); // Text input
        questionQueue.offer(new ListerPreferenceActivity.Question("Landmarks nearby:", "landmarks", true)); // Text input
        optionsList.put("Landmarks nearby:", new ArrayList<>(Arrays.asList("near to school", "near to university", "near to bus stop", "near to grocery store")));// Text input
        askNextQuestion();

        buttonContinue = findViewById(R.id.button);

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to start ListerHomePageActivity
                Intent intent = new Intent(ListerPreferenceActivity.this, ListerHomePageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void askNextQuestion() {
        if (!questionQueue.isEmpty()) {
            ListerPreferenceActivity.Question question = questionQueue.peek();

            askQuestion(question);
        } else {
            for (Map.Entry<String, String> entry : responses.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            //add to rds
            sendResponsesToLambda();
        }
    }

    private void askQuestion(ListerPreferenceActivity.Question question) {
//        addMessage("System", "Rewrite the following: " + question.getQuestion());
        new ListerPreferenceActivity.OpenAIRequestTask().execute(question.getQuestion());
    }

    private void addButton(String option, ListerPreferenceActivity.Question question) {
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
    private void addTextInput(ListerPreferenceActivity.Question question) {
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
                    ListerPreferenceActivity.OpenAIClassifyRequestTask task  = new ListerPreferenceActivity.OpenAIClassifyRequestTask();
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
                jsonObject.put("prompt", "This is a question to property leaser, so rewrite accordingly for better understanding: "+prompt);
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
                        ListerPreferenceActivity.Question questionTemp = questionQueue.poll();
                        //Check question to send to gpt for filtering
                        addTextInput(questionTemp);
                        // check user input class
                    }else{
                        Log.e("DEBUGV", String.valueOf(questionQueue.peek().getQuestion()));
                        Log.e("DEBUGV", String.valueOf(questionQueue.peek().getOptions()));
                        ListerPreferenceActivity.Question questionTemp = questionQueue.poll();
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

            JSONObject jsonData = new JSONObject();
            responses.put("photos","Sample.png");
            String userId = getIntent().getStringExtra("USER_ID");
            Log.e("Lambda Function", userId);
            responses.put("user_id",userId);
            if(!responses.containsKey("landmarks")){
                responses.put("landmarks","near to university");
            }
            JSONObject userResponses = new JSONObject(responses); // Convert responses HashMap to JSONObject

            // Add user responses to the JSON data
            jsonData.put("data", userResponses);
            Log.e("LambdaFunction", "Debugging: " + jsonData);
            // Lambda function URL
            String lambdaUrl = "https://d0awn163r2.execute-api.us-east-2.amazonaws.com/default/CMPE277";

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

