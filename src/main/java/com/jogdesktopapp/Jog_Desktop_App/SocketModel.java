package com.jogdesktopapp.Jog_Desktop_App;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;
import org.json.JSONArray;
import org.json.JSONObject;
import com.jogdesktopapp.Jog_Desktop_App.models.NotificationService;

public class SocketModel extends WebSocketClient {
    private static SocketModel instance;
    private static final String SERVER_URL = "ws://socket.jog-joinourgame.com:8080";
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean connected = false;

    private SocketModel(URI serverUri) {
        super(serverUri);
    }

    public static synchronized SocketModel getInstance() {
        if (instance == null || instance.isClosed()) {
            try {
                instance = new SocketModel(new URI(SERVER_URL));
                instance.connectSocket();
            } catch (Exception e) { 
                e.printStackTrace();
            }
        }
     
        return instance;
    }
    
    void connectSocket(){
    	if(!this.connected) {
            instance.connect();
            return;
    	}
    	return;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("✅ Connected to WebSocket Server");
        boolean oldValue = this.connected;
        this.connected = true;
        propertyChangeSupport.firePropertyChange("connected", oldValue, this.connected);
    	AppFrame.getInstance().updateConnectionStatus(instance.connected);
        GlobalDataClass global = GlobalDataClass.getInstance();
    	String token = global.getToken();
    	// Create JSON object with the token
        JSONObject jsonData = new JSONObject();
        String message = "{\"session_token\": \"" + token + "\"}";
        jsonData.put("session_token", token);
        System.out.println("Message  : " + message);
        instance.send(message);
        System.out.println("TOKEN is  : " + token);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 Received: " + message);
        SwingUtilities.invokeLater(() -> handleAction(message));
    }
    
    

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Disconnected: " + reason);
        boolean oldValue = this.connected;
        this.connected = false;
        propertyChangeSupport.firePropertyChange("connected", oldValue, this.connected);
        reconnect();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("⚠️ Error: " + ex.getMessage());
        boolean oldValue = this.connected;
        this.connected = false;
        propertyChangeSupport.firePropertyChange("connected", oldValue, this.connected);
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

    // Property change support methods
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private void handleAction(String message) {
        String action = parseAction(message);
        JSONObject data = new JSONObject(message);
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);

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
                    App.globalData.sftpClient.addFiles(pendingFiles); 
                } else {
                    System.out.println("⚠️ No file paths or file IDs found.");
                }
                break;
                
            case "redesign_request":
                GlobalDataClass globalData = GlobalDataClass.getInstance();
                globalData.getRedesignDownloadingData(1, oneYearAgo, today); 
                int designerId = data.getInt("designer_id");
                String designerName = data.getString("designer_name");
                
                NotificationService.showNotification(
                    "Redesign Request", 
                    "A redesign request has been received. Assigned Designer: " + designerName + "."
                ); 
                break;
                
//            case "reprint_notification":
//                GlobalDataClass globalDat = GlobalDataClass.getInstance();
//                globalDat.getReprintDownloadingData(1, oneYearAgo, today);
//                int fileId = data.getInt("file_id");
//                long barcode = data.getLong("barcode");
//                String printerName = data.getString("printer_name");
//                
//                NotificationService.showNotification(
//                    "Reprint Notification",
//                    "A reprint request has been received. Printer: " + printerName + "."
//                );
//                break;
                
            case  "reprint_request":
            	GlobalDataClass globalData2 = GlobalDataClass.getInstance();
                globalData2.getReprintDownloadingData(1, oneYearAgo, today);
                int fileId2 = data.getInt("file_id");
                long barcode2 = data.getLong("barcode");
                String printerName2 = data.getString("printer_name");
                
                NotificationService.showNotification(
                    "Reprint Notification",
                    "A reprint request has been received. Printer: " + printerName2 + "."
                );
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