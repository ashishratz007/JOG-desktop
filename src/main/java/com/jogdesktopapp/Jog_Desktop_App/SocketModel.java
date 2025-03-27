package com.jogdesktopapp.Jog_Desktop_App;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jogdesktopapp.Jog_Desktop_App.models.NotificationService;

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
        SwingUtilities.invokeLater(() -> handleAction(message));
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
            Thread.sleep(3000);
            getInstance();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void handleAction(String message) {
        String action = parseAction(message);
        JSONObject data = new JSONObject(message);
        
        switch (action) {
            case "upload_files":
                System.out.println("📤 Handling file upload action");
                JSONObject json = new JSONObject(message);
                JSONArray filePathsArray = json.optJSONArray("file_paths");
                JSONArray fileIdsArray = json.optJSONArray("file_ids");
                String orderCode = json.optString("order_code", "");

                System.out.println("📤 Keys are: " + orderCode);

                if (filePathsArray != null && fileIdsArray != null) {
                    List<UploadFile> pendingFiles = new ArrayList<>();
                    for (int i = 0; i < filePathsArray.length(); i++) {
                        String filePath = filePathsArray.optString(i);
                        String fileId = fileIdsArray.optString(i);
                        UploadFile fileData = new UploadFile(fileId, filePath, "pending", orderCode);
                        pendingFiles.add(fileData);
                        System.out.println("📤 adding file");
                    }
                    App.sftpClient.addFiles(pendingFiles); 
                } else {
                    System.out.println("⚠️ No file paths or file IDs found.");
                }
                break;
                
            case "redesign_request":
                
                
                GlobalDataClass globalData = GlobalDataClass.getInstance();
                globalData.getRedesignData(1); 

                int designerId = data.getInt("designer_id");
                String designerName = data.getString("designer_name");
                
                NotificationService.showNotification(
                    "Redesign Request", 
                    "A redesign request has been received. Assigned Designer: " + designerName + "."
                ); 
             // Update redesign count
                AppFrame appFrame = AppFrame.getInstance();
                appFrame.setRedesignCount(appFrame.getRedesignCount() + 1);
                break;
                
            case "reprint_notification":
               
                
                GlobalDataClass globalDat = GlobalDataClass.getInstance();
                globalDat.getReprintData(1);
                
                int fileId = data.getInt("file_id");
                long barcode = data.getLong("barcode");
                String printerName = data.getString("printer_name");
                
                NotificationService.showNotification(
                    "Reprint Notification",
                    "A reprint request has been received. Printer: " + printerName + "."
                );
             // Update reprint count
                AppFrame frame = AppFrame.getInstance();
                frame.setReprintCount(frame.getReprintCount() + 1);
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