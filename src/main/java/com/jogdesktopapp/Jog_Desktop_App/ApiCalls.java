package com.jogdesktopapp.Jog_Desktop_App;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ApiCalls {
    
    public static String confirmUpload(String id, String path,  String base64Image) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/update_synology.php";
        String jsonInputString = "{\"order_id\": \"" + id + "\", \"synology_path\": \"" + path + "\", \"image\": \"" + base64Image + "\"}";
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
            	System.out.println("Path setup sucess");
                return "Success: " + responseCode;
            } else {
            	System.out.println("Path setup Error");
                return "Failed: " + responseCode;
            }
        } catch (Exception e) {
        	System.out.println("Path setup Error");
            return "Error: " + e.getMessage();
        }
    }
    
    // delete file if no exits
    public static boolean deleteFile(String id) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/delete_files.php";
        String jsonInputString = "{\"order_id\": \"" + id + "\"}";
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
            	System.out.println("File removed");
                return true;
            } else {
            	System.out.println("File remove error");
                return false;
            }
        } catch (Exception e) {
        	System.out.println("File remove Error");
            return false;
        }
    }
    
    // Get pending files from API
    public static List<UploadFile> getPendingFiles() {
        List<UploadFile> pendingFiles = new ArrayList<>();
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/get_pending_files.php";
        
        try {
            // Create HTTP Connection
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Check response code
            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch files! HTTP error code: " + conn.getResponseCode());
                return pendingFiles;
            }

            // Read response
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            // Parse JSON response
            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
            	
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                UploadFile uploadFile = UploadFile.fromJson(jsonObj);
                pendingFiles.add(uploadFile);
            }

        } catch (Exception e) {
            System.out.println("❌ Error fetching pending files: " + e.getMessage());
        }

        return pendingFiles;
    }
 
}