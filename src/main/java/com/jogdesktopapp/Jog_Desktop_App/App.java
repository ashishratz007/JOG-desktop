package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;
import java.net.Socket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class App {
	 // Initialize socket connection
    static SocketModel socketModel = SocketModel.getInstance();
//    static SynologyServerModel synologyServer = SynologyServerModel.getInstance(); // Initialize the singleton
    static SftpUploader sftpClient = SftpUploader.getInstance(); // actions client for upload download and many more functions 
    public static void main(String[] args) {
    	PermissionRequest.main(args); 
        EventQueue.invokeLater(() -> {
            try {
//                synologyServer.init(); // Now it won't be null
                AppFrame window = new AppFrame();
                window.setVisible(true);
                sftpClient.getPendingFiles();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
