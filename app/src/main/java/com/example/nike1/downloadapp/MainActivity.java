package com.example.nike1.downloadapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    int[] idButtons = {R.id.button1, R.id.button2, R.id.button3, R.id.button4};
    Celebrity[] listOfCelebrities;
    int randCelebrity, randPos, availableButtons = 4;
    ArrayList<Integer> remainingCelebrities;
    Button playAgainButton;

    public class DownloadResources extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection httpURLConnection;
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            StringBuilder stringBuilder = new StringBuilder();
            String line;
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
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class Celebrity {
        private final String name;
        private final String source;
        private boolean used = false;

        public Celebrity(String xName, String xSource) {
            name = xName;
            source = xSource;
        }

        public String getName() {
            return name;
        }

        public String getSource() {
            return source;
        }

        public boolean isUsed() {
            return used;
        }

        public void markAsUsed() {
            used = true;
        }

        public void markAsUnused() {
            used = false;
        }
    }

    public void nextCelebrity(View view) {
        if (view.getId() != R.id.imageView) {
            Button lastButton = (Button) view;
            if (listOfCelebrities[randCelebrity].getName() == lastButton.getText()) {
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wrong that's " + listOfCelebrities[randCelebrity].getName() + "!", Toast.LENGTH_SHORT).show();
            }
            if (remainingCelebrities.size() <= 3) {
                availableButtons--;
                lastButton = findViewById(idButtons[availableButtons]);
                lastButton.setVisibility(View.INVISIBLE);
            }
        }
        if (availableButtons > 0) {
            do {
                randCelebrity = new Random().nextInt(100);
            } while (listOfCelebrities[randCelebrity].isUsed());
            randPos = new Random().nextInt(availableButtons);

            ImageDownloader imageDownloader = new ImageDownloader();
            Bitmap randPhoto = null;
            try {
                randPhoto = imageDownloader.execute(listOfCelebrities[randCelebrity].getSource()).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(randPhoto);
            listOfCelebrities[randCelebrity].markAsUsed();
            remainingCelebrities.remove(remainingCelebrities.indexOf(randCelebrity));

            ArrayList<Integer> selectedIndexes = new ArrayList<>();
            selectedIndexes.add(randCelebrity);
            int tempIndex;
            for (int i = 0; i < availableButtons; i++) {
                Button button = findViewById(idButtons[i]);
                if (i == randPos) {
                    button.setText(listOfCelebrities[randCelebrity].getName());
                } else {
                    do {
                        tempIndex = new Random().nextInt(100);
                    }
                    while (selectedIndexes.contains(tempIndex) || !remainingCelebrities.contains(tempIndex));
                    selectedIndexes.add(tempIndex);
                    button.setText(listOfCelebrities[tempIndex].getName());
                }
            }
        } else {
            imageView.setVisibility(View.INVISIBLE);
            playAgainButton.setVisibility(View.VISIBLE);
        }
    }

    public void restart(View view) {
        availableButtons = 4;
        playAgainButton.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.VISIBLE);
        for (int idButton : idButtons) {
            Button button = findViewById(idButton);
            button.setVisibility(View.VISIBLE);
        }
        for (Celebrity celebrity : listOfCelebrities) {
            celebrity.markAsUnused();
        }
        remainingCelebrities.clear();
        for (int i = 0; i < 100; i++) {
            remainingCelebrities.add(i);
        }
        nextCelebrity(imageView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        playAgainButton = findViewById(R.id.playAgainButton);

        DownloadResources downloader = new DownloadResources();
        String matchedString, result = "";
        Pattern extPattern, namePattern, sourcePattern;
        Matcher extMatcher, nameMatcher, sourceMatcher;
        listOfCelebrities = new Celebrity[100];
        int pos = 0;
        final String TAG = "TestApp";

        try {
            result = downloader.execute("https://www.imdb.com/list/ls052283250/").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        extPattern = Pattern.compile("<div class=\"lister-item-image\">(.*?)</a>        </div>");
        namePattern = Pattern.compile("img alt=\"(.*?)\"");
        sourcePattern = Pattern.compile("src=\"(.*?)\"");
        extMatcher = extPattern.matcher(result);
        while (extMatcher.find()) {
            String tempName, tempSoruce;
            matchedString = extMatcher.group(1);

            nameMatcher = namePattern.matcher(matchedString);
            sourceMatcher = sourcePattern.matcher(matchedString);
            if (nameMatcher.find() && sourceMatcher.find()) {
                tempName = nameMatcher.group(1);
                tempSoruce = sourceMatcher.group(1);
                Celebrity tempCelebrity = new Celebrity(tempName, tempSoruce);
                listOfCelebrities[pos++] = tempCelebrity;
                Log.i(TAG, listOfCelebrities[pos - 1].getName() + " " + listOfCelebrities[pos - 1].getSource());
            }
        }

        remainingCelebrities = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            remainingCelebrities.add(i);
        }
        nextCelebrity(imageView);
    }
}
