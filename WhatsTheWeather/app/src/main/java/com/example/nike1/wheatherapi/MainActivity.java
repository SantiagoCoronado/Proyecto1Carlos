package com.example.nike1.wheatherapi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText cityNameEditText;
    TextView resultTextView;

    public class JsonDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String line;
            URL url;
            HttpURLConnection httpURLConnection;
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (inputStreamReader != null) inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (bufferedReader != null) bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("TestApp", result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                String weatherInfo = jsonObject.getString("weather");
                Log.i("TestApp", weatherInfo);
                JSONArray jsonArray = new JSONArray(weatherInfo);
                JSONObject jsonPart = jsonArray.getJSONObject(0);
                Log.i("TestApp", jsonPart.getString("main"));
                Log.i("TestApp", jsonPart.getString("description"));
                String output = "Weather: " + jsonPart.getString("main") +
                        "\nDescription: " + jsonPart.getString("description") + "\n";

                jsonPart = jsonObject.getJSONObject("main");
                output += "Temp: " + jsonPart.getString("temp") +
                        " K\nPressure: " + jsonPart.getString("pressure") +
                        " hPa\nHumidity: " + jsonPart.getString("humidity") +
                        " %\nTemp Min: " + jsonPart.getString("temp_min") +
                        " K\nTemp Max: " + jsonPart.getString("temp_max") + " K";

                resultTextView.setText(cityNameEditText.getText().toString() + "\n" + output);
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "Unable to download information, please check spell or try again :)", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void getWeather(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(cityNameEditText.getWindowToken(), 0);

        String text = cityNameEditText.getText().toString();
        if (!text.equals("")) {
            try {
                String encodedCityName = URLEncoder.encode(text, "UTF-8");
                Log.i("TestApp", encodedCityName);
                JsonDownloader jsonDownloader = new JsonDownloader();
                jsonDownloader.execute(getString(R.string.api_part1) + encodedCityName + getString(R.string.api_part2));
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(MainActivity.this, "Unable to download information, please check spell or try again :)", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameEditText = findViewById(R.id.cityNameEditText);
        resultTextView = findViewById(R.id.resultTextView);
    }
}
