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
    
	// confirm your upload to synology server
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
        String jsonInputString = "{\"id\": \"" + id + "\"}";
        
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
 
    // get reprint list items
    public static ReprintModel getReprintList(int status, int limit, int page, String startDate, String endDate) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/mobile/get_reprint_list.php";
        ReprintModel reprintModel = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject postData = new JSONObject();
            postData.put("status", status);
            postData.put("limit", limit);
            postData.put("page", page);
            postData.put("start_date", startDate);
            postData.put("end_date", endDate);
            
            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch reprint list! HTTP error code: " + conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            reprintModel = ReprintModel.fromJson(jsonResponse);
        } catch (Exception e) {
            System.out.println("❌ Error fetching reprint list: " + e.getMessage());
        }

        return reprintModel;
    }
    // get reprint list items
    public static RedesignModel getRedesignList(int status, int limit, int page, String startDate, String endDate) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/mobile/get_redesign_list.php";
        RedesignModel redesignModel = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject postData = new JSONObject();
            postData.put("status", status);
            postData.put("limit", limit);
            postData.put("page", page);
            postData.put("start_date", startDate);
            postData.put("end_date", endDate);
            
            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch reprint list! HTTP error code: " + conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            redesignModel = RedesignModel.fromJson(jsonResponse);
        } catch (Exception e) {
            System.out.println("❌ Error fetching redesigner list: " + e.getMessage());
        }

        return redesignModel;
    }

	// confirm your pending file status
    public static String markComplete(boolean isReprint, String fileId) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/mobile/update_status.php";
        String table_name = isReprint? "reprint" :"redeign";
        String jsonInputString = "{\"table_name\": \"" + "reprint" + "\", \"rep_id\": \"" + fileId + "\", \"status\": \"" + "1" + "\"}";
        
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
    
   // downloaded file
    public static DownloadedFilesModel getDownlaodedList( int limit, int page) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/get_downloaded_files.php";
        DownloadedFilesModel downlodedModel = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject postData = new JSONObject();
            postData.put("limit", limit);
            postData.put("page", page);

            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch reprint list! HTTP error code: " + conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            downlodedModel = DownloadedFilesModel.fromJson(jsonResponse);
        } catch (Exception e) {
            System.out.println("❌ Error fetching dwonloaded list: " + e.getMessage());
        }

        return downlodedModel;
    }
    
   // Uploaded file
    public static UploadedFilesModel getUploadedList( int limit, int page) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/get_uploaded_files.php";
        UploadedFilesModel uploadedModel = null; 

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject postData = new JSONObject();
            postData.put("limit", limit);
            postData.put("page", page);

            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch reprint list! HTTP error code: " + conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();

            JSONObject jsonResponse = new JSONObject(response.toString());
            uploadedModel = UploadedFilesModel.fromJson(jsonResponse);
        } catch (Exception e) {
            System.out.println("❌ Error fetching upladed list: " + e.getMessage());
        }

        return uploadedModel;
    }

	// confirm your download to local system
    public static String confirmDownload(String id) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/update_file_download.php";
        String jsonInputString = "{\"file_id\": \"" + id + "\",}";
        
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
                throw new Error("Failed: " + responseCode);
            }
        } catch (Exception e) {
        	System.out.println("Path setup Error");
            throw new Error("Error: " + e.getMessage());
        }
    }
    
}