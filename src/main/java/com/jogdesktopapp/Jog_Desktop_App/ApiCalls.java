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
    
	// login to user account
    public static String login(String username, String password,  String serialnumber) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/login_adobe.php";
        String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\", \"serialnumber\": \"" + serialnumber + "\"}"; 
        
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
         // Read response
            String response;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                response = br.readLine();
            }
            System.out.println("Response is : " + response);
            if (responseCode == HttpURLConnection.HTTP_OK && !response.contains("error")) {
            	System.out.println("Login done");
            	GlobalDataClass.getInstance().saveTokenToDesktop(response);
                return "sucess";
            } else {
            	String error = "User already logged in from another device";
            	if(response.contains("Invalid")) {
            		error = "Invalid credentials";
            	}
            	System.out.println(error);
                throw new Exception(error); 
            }
        } catch (Exception e) {
        	  System.out.println("Login Error");
              return e.getMessage();
        }
    }
	
    // login to user account
    public static boolean logout(String token) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/logout_adobe.php";
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", token);
            conn.setDoOutput(true);
            

         // Send empty JSON body (or remove if not needed)
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = "{}".getBytes("utf-8");
                os.write(input, 0, input.length);
            }


            int responseCode = conn.getResponseCode();
         // Read response
            String response;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                response = br.readLine();
            }
            System.out.println("Response is : " + response);
            if (responseCode == HttpURLConnection.HTTP_OK && !response.contains("error")) {
            	System.out.println("Login done");
                return true;
            } else {
            	String error = "Logout Error";
            	if(response.contains("Invalid")) {
            		error = "Invalid credentials";
            	}
            	System.out.println(error);
                throw new Exception(error); 
            }
        } catch (Exception e) {
        	  System.out.println("Login Error");
              return false;
        }
    }
	
	// confirm your upload to synology server
    public static String confirmUpload(String id, String path,  String base64Image) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/update_synology.php";
        String jsonInputString = "{\"order_id\": \"" + id + "\", \"synology_path\": \"" + path + "\", \"image\": \"" + base64Image + "\"}";
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
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
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
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
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
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
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", "Bearer " + token);
          
            System.out.println("⚠️ Header Token: Bearer " + token);
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
   
    // get reprint pending task list items
    public static ReprintPendingModel getReprintPendingList(int status, int limit, int page, String startDate, String endDate) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/get_reprint_files.php";
        ReprintPendingModel reprintModel = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
          
            System.out.println("⚠️ Header Token: Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject postData = new JSONObject();
            postData.put("is_downloaded", 1);
            postData.put("limit", limit);
            postData.put("page", page);
            postData.put("start_date", startDate);
            postData.put("end_date", endDate);
            
            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch pending reprint list! HTTP error code: " + conn.getResponseCode());
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
            reprintModel = ReprintPendingModel.fromJson(jsonResponse);
        } catch (Exception e) {
            System.out.println("❌ Error fetching pending reprint list: " + e.getMessage());
        }

        return reprintModel;
    }
   
    // get redesign list items
    public static RedesignPendingModel getDesignPendingList(int status, int limit, int page, String startDate, String endDate) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/get_redesign_files.php";
        RedesignPendingModel redesignModel = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
          
            System.out.println("⚠️ Header Token: Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject postData = new JSONObject();
            postData.put("is_downloaded", 1);
            postData.put("limit", limit);
            postData.put("page", page);
            postData.put("start_date", startDate);
            postData.put("end_date", endDate);
            
            OutputStream os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();
            os.close();

            if (conn.getResponseCode() != 200) {
                System.out.println("⚠️ Failed to fetch pending redesign list! HTTP error code: " + conn.getResponseCode());
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
            redesignModel = RedesignPendingModel.fromJson(jsonResponse);
        } catch (Exception e) {
            System.out.println("❌ Error fetching pending redesign list: " + e.getMessage());
        }

        return redesignModel;
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
        String token = GlobalDataClass.getInstance().getToken();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        System.out.println("⚠️ Redesign Token: Bearer " + token); // Debug token directly
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject postData = new JSONObject();
        postData.put("status", status);
        postData.put("limit", limit);
        postData.put("page", page);
        postData.put("start_date", startDate);
        postData.put("end_date", endDate);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.toString().getBytes());
        }

        if (conn.getResponseCode() != 200) {
            System.out.println("⚠️ Failed! HTTP " + conn.getResponseCode());
            try (BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                err.lines().forEach(System.out::println);
            }
            return null;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            JSONObject jsonResponse = new JSONObject(response.toString());
            redesignModel = RedesignModel.fromJson(jsonResponse);
        }
    } catch (Exception e) {
        System.out.println("❌ Error: " + e.getMessage());
    } finally {
    }
    return redesignModel;
}
	// confirm your pending file status
    public static String markComplete(boolean isReprint, String fileId) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/mobile/update_status.php";
        String table_name = isReprint? "reprint" :"redesign";
        System.out.println("actions to :  "  + isReprint  +  "=====" + fileId);
        String jsonInputString = "{\"table_name\": \"" + table_name + "\", \"rep_id\": \"" + fileId + "\", \"status\": \"" + "1" + "\"}";
        
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
            	System.out.println("action done sucess");
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
    public static DownloadedFilesModel getDownloadedList( int limit, int page) {
        String apiUrl = "https://jog-desktop.jog-joinourgame.com/get_downloaded_files.php";
        DownloadedFilesModel downlodedModel = null;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
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
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
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
       
        JSONObject postData = new JSONObject();
        postData.put("file_id", id);
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            String token = GlobalDataClass.getInstance().getToken();
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
            	System.out.println("download status setup sucess");
                return "Success: " + responseCode;
            } else {
            	System.out.println("download status setup Error");
                throw new Error("Failed: " + responseCode);
            }
        } catch (Exception e) {
        	System.out.println("download status setup Error");
            throw new Error("Error: " + e.getMessage());
        }
    }
    
}