package com.jogdesktopapp.Jog_Desktop_App;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SynologyServerModel {
    private static SynologyServerModel instance;
    private static final String SERVER_IP = "192.168.88.186";
    private static final String USERNAME = "Synology0822";
    private static final String PASSWORD = "Install0822";
    private static final String API_URL = "http://" + SERVER_IP + ":5000/webapi/";
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

    public void init() {
        System.out.println("Initializing connection to Synology NAS...");
        isConnected = authenticate();
    }

    private boolean authenticate() {
        String loginUrl = API_URL + "auth.cgi?api=SYNO.API.Auth&method=login&version=2"
                + "&account=" + USERNAME + "&passwd=" + PASSWORD 
                + "&session=FileStation&format=sid";

        Request request = new Request.Builder().url(loginUrl).get().build();

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
    
    static void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                sendFileToNas(selectedFile);
                JOptionPane.showMessageDialog(null, "File uploaded successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error uploading file: " + ex.getMessage());
            }
        }
    }

    private static void sendFileToNas(File file) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost("http://192.168.88.186:5000/webapi/entry.cgi?api=SYNO.FileStation.Upload&method=upload&version=2");
            httppost.addHeader("Cookie", "id=" + getInstance().sessionId);

            FileBody fileBody = new FileBody(file);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("overwrite", new StringBody("false", org.apache.http.entity.ContentType.TEXT_PLAIN))
                    .addPart("path", new StringBody(FOLDERPATH, org.apache.http.entity.ContentType.TEXT_PLAIN))
                    .addPart("create_parents", new StringBody("true", org.apache.http.entity.ContentType.TEXT_PLAIN))
                    .addPart("filename", fileBody)
                    .setLaxMode()
                    .build();

            httppost.setEntity(reqEntity);

            System.out.println("Executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    System.out.println(EntityUtils.toString(resEntity));
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    public List<String> getFilesystem() {
        List<String> fileList = new ArrayList<>();

        String listUrl = FILE_API + "?api=SYNO.FileStation.List&version=2&method=list"
                + "&folder_path=" + FOLDERPATH + "&_sid=" + sessionId;

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

 
