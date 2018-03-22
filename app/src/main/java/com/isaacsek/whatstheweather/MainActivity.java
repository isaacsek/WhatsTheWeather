package com.isaacsek.whatstheweather;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView resultTextView;
    Handler handler;
    Button button;

    @Override
    // First called when app starts up
    protected void onCreate(Bundle savedInstanceState) {
        // boilerplate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // init global variables
        editText = findViewById(R.id.editText);
        resultTextView = findViewById(R.id.resultTextView);
        handler = new Handler(Looper.getMainLooper());
        button = findViewById(R.id.button);

        // Create listender for textbox, click button when enter is clicked
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || ((event.getAction() == KeyEvent.ACTION_DOWN) && (actionId == KeyEvent.KEYCODE_ENTER))) {
                    button.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    // method that grabs a city name from the input text, and makes api call
    // to openweather, and returns the result
    public void getWeather(View view) {
        // Hide keyboard below when entering input
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        try {
            // create new taskk
            DownloadTask task = new DownloadTask();
            String encodedCityName = URLEncoder.encode(editText.getText().toString(), "UTF-8");

            // execute the task.doInBackground()
            task.execute("http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&APPID=a0a84d1db41f536213145f023145bb1b");

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Could not find weather :(",Toast.LENGTH_SHORT).show();
        }
    }

    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        // DownloadTask.execute(String 1, String 2) // String... acts like an array
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                // Establish URL connection
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                // Read in data from http connection, char by char
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Throwable e) {
                e.printStackTrace();

                // Dont run UI procecc in the background thread
                // Do UI stuffs in main/UI thread. Need to research threads in android
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Could not find weather :(",Toast.LENGTH_SHORT).show();
                        resultTextView.setText("Invalid city name.");
                    }
                });

                // this method returns a string, so much return something
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                // convert string to JSON and get key value
                JSONObject jsonObject = new JSONObject(s);
                String weatherInfo = jsonObject.getString("weather");

                Log.i("Weather content", weatherInfo);

                // convert string array to JSON array
                JSONArray arr = new JSONArray(weatherInfo);

                // loop through array, and add strings to message
                String message = "";
                for (int i=0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");

                    if (!main.equals("") && !description.equals("")) {
                        message += main + ": " + description + "\r\n";
                    }
                }

                // double check for data
                if (!message.equals("")) {
                    resultTextView.setText(message);
                } else {
                    Toast.makeText(getApplicationContext(),"Could not find weather :(",Toast.LENGTH_SHORT).show();
                }

            // triple check
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"Could not find weather :(",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
