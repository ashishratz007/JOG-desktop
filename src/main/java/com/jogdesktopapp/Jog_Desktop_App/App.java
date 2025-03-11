package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.net.Socket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class App {
	 // Initialize socket connection
    static SocketModel socketModel = SocketModel.getInstance();
    
    /// storage server 
    static SynologyServerModel synologyServer = SynologyServerModel.getInstance(); // Initialize the singleton
    
    static SftpUploader sftpClient = SftpUploader.getInstance(); 
    public static void main(String[] args) {
    	PermissionRequest.main(args); 
        EventQueue.invokeLater(() -> {
            try {
//                synologyServer.init(); // Now it won't be null
                AppFrame window = new AppFrame();
                window.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}


 class MyWebSocketClient extends WebSocketClient {

    public MyWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("✅ Connected to WebSocket Server");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 Received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("❌ Disconnected: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("⚠️ Error: " + ex.getMessage());
    }

    public static void main(String[] args) {
        try {
            URI serverUri = new URI("wss://socket.jog-joinourgame.com");  // Change to your WebSocket server URL
            MyWebSocketClient client = new MyWebSocketClient(serverUri);
            client.connect();

            // Wait before sending a message
            Thread.sleep(3000);
            client.send("Hello from Java!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
