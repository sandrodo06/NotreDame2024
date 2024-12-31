package org.feup.apm.notredame.toolbarActivities.exercisesActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.feup.apm.notredame.R;
import org.feup.apm.notredame.main.BaseActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExercisesActivity extends BaseActivity {
    TextView response;
    private VideoAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercises);

        // =========================================================================================
        // Toolbar setup
        setupNavigation();


        // =========================================================================================
        // Video display setup

        // Binds the videos to the RecycleView on the layout
        RecyclerView videoDisplayer = findViewById(R.id.videoDisplayer);

        videoAdapter = new VideoAdapter(this, new ArrayList<>());
        videoDisplayer.setAdapter(videoAdapter);
        videoDisplayer.setLayoutManager(new LinearLayoutManager(this));

        // Fetches the videos from the API
        callAPI(null);

        response = findViewById(R.id.response);
    }




    // =========================================================================================
    // =========================================================================================
    // Helper methods to get the displayed videos

    // Calls the API using the GetVideos method
    public void callAPI(View view){
        Thread thr = new Thread(new GetVideos()); // Runs the API on another thread to avoid long loadings
        thr.start();
    }

    private void writeResponse(final String text) {
        runOnUiThread(() -> response.setText("Unable to get videos"));
    }


    // =========================================================================================
    // =========================================================================================
    // GetVideos class and related functions
    // Used to fetch de videos' data from de API
    private class GetVideos implements Runnable {
        @Override
        public void run() {
            // Definition of variables
            String fetchingId = "PLGqEZc9Dqs60kWVa5KQ6uxayOanXAfBF-";
            String API_KEY = "AIzaSyDjMXkytlQKh3GjvNFb71K2TjGYKJhC1zk";

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet,contentDetails&playlistId=" + fetchingId + "&maxResults=50&key=" + API_KEY); // API Url
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setUseCaches(false);

                int responseCode = urlConnection.getResponseCode();
                if(responseCode == 200) {  // code 200 means OK
                    String resp = readStream(urlConnection.getInputStream()); // reads the JSON API response
                    runOnUiThread(() -> processApiResponse(resp)); // processes the response
                }
                else
                    writeResponse("Code: " + responseCode);
            }
            catch (Exception e) {
                writeResponse(e.toString());
            }
            finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
            }
        }
    }

    // =========================================================================================
    // Reads the data from the API
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder respBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                respBuilder.append(line);
            }
        }
        catch (IOException e) {
            respBuilder.append(e.getMessage());
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    respBuilder.append(e.getMessage());
                }
            }
        }
        return respBuilder.toString();
    }

    // =========================================================================================
    // Processes the data from the JSON API response, keeping only the useful information:
    // 1) Videos' Ids
    // 2) Videos' titles
    // 3) Videos' descriptions
    // 4) Videos' thumbnails
    private void processApiResponse(String apiResponse) {
        try {
            JSONObject jsonObject = new JSONObject(apiResponse);
            JSONArray itemsArray = jsonObject.getJSONArray("items");

            // Goes through every video retrieved from the API
            if (itemsArray.length() > 0) {
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject videoObject = itemsArray.getJSONObject(i).getJSONObject("snippet"); // All the desired information is on the snippet section of each item

                    // Desired information
                    String id = videoObject.getJSONObject("resourceId").getString("videoId");

                    String title = videoObject.getString("title");
                    title = title.split("//")[0].trim(); // Gets the title up until "//"

                    String description = videoObject.getString("description");

                    JSONObject thumbnails = videoObject.getJSONObject("thumbnails");
                    String thumbnailAddress = getBestThumbnail(thumbnails); // Gets the highest quality thumbnail available

                    // Adds videos to the video lis
                    VideoItem video = new VideoItem(title, description, thumbnailAddress, id);
                    videoAdapter.addVideo(video); // Adds video to the adapter
                }

            } else {
                writeResponse("No videos found.");
            }
        } catch (JSONException e) {
            writeResponse("Error parsing JSON: " + e.getMessage());
        }
    }


    // =========================================================================================
    // Helper method

    // Gets the highest quality thumbnail available
    private String getBestThumbnail(JSONObject thumbnails) {
        String[] availableQuality = {"maxres", "high", "medium", "standard", "default"};

        // Loop through available resolutions to get the best available thumbnail
        for (String resolution : availableQuality) {
            if (thumbnails.has(resolution)) {
                try {
                    return thumbnails.getJSONObject(resolution).getString("url");
                }
                catch (JSONException e) {
                    writeResponse("Error parsing JSON: " + e.getMessage());
                }
            }
        }

        return "";
    }
}
