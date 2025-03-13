package com.jogdesktopapp.Jog_Desktop_App;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiCalls {
    
    public static String confirmUpload(String id, String path) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/update_synology.php";
        String jsonInputString = "{\"order_id\": \"" + id + "\", \"synology_path\": \"" + path + "\"}";
        
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
}