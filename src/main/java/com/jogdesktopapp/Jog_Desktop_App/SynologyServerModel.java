package com.jogdesktopapp.Jog_Desktop_App;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.*;
import java.nio.file.Files;

public class SynologyServerModel {
    private static SynologyServerModel instance;
    private static final String SERVER_IP = "192.168.88.186";
    private static final String USERNAME = "Synology0822";
    private static final String PASSWORD = "InstallSUB2025";
    static final String UPLOADPATH = "http://192.168.88.186:5000/webapi/entry.cgi?api=SYNO.FileStation.Upload&method=upload&version=2&method=upload&path=/jog%208tb/JOG%20India";
    private static final String API_URL = "http://" + SERVER_IP + ":5000/webapi.cgi/";
    private static final String FILE_API = API_URL + "entry.cgi";
    private static final String FOLDERPATH = "/jog%208tb/JOG%20India";

    private String sessionId;
    boolean isConnected;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    private SynologyServerModel() {
        client = new OkHttpClient();
        objectMapper = new ObjectMapper();
    }

    public static synchronized SynologyServerModel getInstance() {
        if (instance == null) {
            instance = new SynologyServerModel();
        }
        return instance;
    }

    /**
     * Initializes connection to Synology NAS.
     */
    public void init() {
        System.out.println("Initializing connection to Synology NAS...");
        isConnected = authenticate();
    }

    /**
     * Authenticates the user and establishes a session.
     */
    private boolean authenticate() {
 	   String loginUrl = "http://192.168.88.186:5000/webapi/entry.cgi?api=SYNO.API.Auth&version=6&method=login&account="+USERNAME + "&passwd="+ "InstallSUB2025"+ "&session=FileStation&format=sid";
 	   
        Request request = new Request.Builder().url(loginUrl).get().build();
        System.out.println("Initializing connection to Synology NAS...");
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.body().string());

                if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
                    sessionId = jsonResponse.get("data").get("sid").asText();
                    System.out.println("Connected to Synology NAS. Session ID: " + sessionId);
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to Synology NAS: " + e.getMessage());
        }

        System.err.println("Failed to authenticate.");
        return false;
    }
    
    /**
     * Uploads a file to the NAS inside the specified file path folder.
     */
    public  void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                sendFileToNas(selectedFile.getName(), fileData);
                JOptionPane.showMessageDialog(null, "File uploaded successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error uploading file: " + ex.getMessage());
            }
        }
    }

    private static void sendFileToNas(String fileName, byte[] fileData) {
        OkHttpClient client = new OkHttpClient();
        
        RequestBody fileBody = RequestBody.create(fileData, MediaType.parse("application/octet-stream"));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("api", "SYNO.FileStation.Upload")
                .addFormDataPart("version", "2")
                .addFormDataPart("method", "upload")
                .addFormDataPart("_sid", getInstance().sessionId)
                .addFormDataPart("path", FOLDERPATH)
                .addFormDataPart("create_parents", "true")
                .addFormDataPart("file", fileName, fileBody)
                .build();
        
        Request request = new Request.Builder()
                .url(UPLOADPATH)
                .post(requestBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                System.out.println("File uploaded successfully: " + fileName);
            } else {
                System.err.println("Failed to upload file: " + fileName);
            }
            System.err.println("Server response is :" + response.body());
        } catch (IOException e) {
        	
            System.err.println("Error uploading file: " + e.getMessage());
        }
    }
    
    
    /**
     * Fetches file/folder list from the NAS.
     */
    public List<String> getFilesystem() {
    	 System.out.println("Entred into the get files");
        List<String> fileList = new ArrayList<>();

        String listUrl = "http://192.168.88.186:5000/webapi/entry.cgi?api=SYNO.FileStation.List&version=2&method=list&folder_path=/jog%208tb/JOG%20India" + "&_sid=" + sessionId;

        Request request = new Request.Builder().url(listUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.body().string());

                if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
                    JsonNode files = jsonResponse.get("data").get("files");

                    for (JsonNode file : files) {
                        fileList.add(file.get("name").asText());
                    }

                    System.out.println("Fetched " + fileList.size() + " items from NAS.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error fetching file list: " + e.getMessage());
        }

        return fileList;
    }

    /**
     * Logs out from the Synology NAS session.
     */
    public void logout() {
        if (sessionId == null) {
            System.out.println("No active session to logout.");
            return;
        }

        String logoutUrl = API_URL + "auth.cgi?api=SYNO.API.Auth&method=logout&version=2&session=FileStation";

        Request request = new Request.Builder().url(logoutUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Logged out from Synology NAS.");
                sessionId = null;
                isConnected = false;
            }
        } catch (IOException e) {
            System.err.println("Error logging out: " + e.getMessage());
        }
    }
}


 class FileUtils {
    public static boolean writeToFile(InputStream inputStream, File file) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            return false;
        }
    }
}

 
