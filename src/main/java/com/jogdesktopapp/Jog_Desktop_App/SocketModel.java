package com.jogdesktopapp.Jog_Desktop_App;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SocketModel extends WebSocketClient {
    private static SocketModel instance;
    private static final String SERVER_URL = "ws://socket.jog-joinourgame.com:8080";

    private SocketModel(URI serverUri) {
        super(serverUri);
    }

    public static synchronized SocketModel getInstance() {
        if (instance == null || instance.isClosed()) {
            try {
                instance = new SocketModel(new URI(SERVER_URL));
                instance.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("✅ Connected to WebSocket Server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 Received: " + message);
        handleAction(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Disconnected: " + reason);
        reconnect();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("⚠️ Error: " + ex.getMessage());
    }

    public void reconnect() {
        System.out.println("🔄 Attempting to reconnect...");
        try {
            Thread.sleep(3000); // Wait before reconnecting
            getInstance();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void handleAction(String message) {
        String action = parseAction(message);
        switch (action) {
            case "upload_files":
            
                System.out.println("📤 Handling file upload action");
                JSONObject json = new JSONObject(message);
                JSONArray filePathsArray = json.optJSONArray("file_paths");
                JSONArray fileIdsArray = json.optJSONArray("file_ids");
                JSONArray orderKeys = json.optJSONArray("order_code"); 
                System.out.println("📤 Keys are:  " + orderKeys);

                if (filePathsArray != null && fileIdsArray != null) {
                	List<UploadFile> pendingFiles = new ArrayList<>();
                    for (int i = 0; i < filePathsArray.length(); i++) {
                        String filePath = filePathsArray.optString(i);
                        String fileId = fileIdsArray.optString(i);
                        UploadFile fileData = new UploadFile(fileId, filePath, "pending");
                        pendingFiles.add(fileData);
                        System.out.println("📤 adding file");
                        
                    }
                    App.sftpClient.addFiles(pendingFiles); 
                } else {
                    System.out.println("⚠️ No file paths or file IDs found.");
                }
               
                break;
            case "ACTION_CONNECT":
                System.out.println("🔗 Handling connect action");
                break;
            case "ACTION_DISCONNECT":
                System.out.println("🔌 Handling disconnect action");
                break;
            case "ACTION_MESSAGE":
                System.out.println("💬 Handling message action");
                break;
            default:
                System.out.println("❓ Unknown action: " + action);
                break;
        }
    }

    private String parseAction(String message) {
        try {
            JSONObject json = new JSONObject(message);
            return json.optString("action", "UNKNOWN");
        } catch (Exception e) {
            e.printStackTrace();
            return "UNKNOWN";
        }
    }
}
