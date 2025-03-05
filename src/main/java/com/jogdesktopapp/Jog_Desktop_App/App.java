package com.jogdesktopapp.Jog_Desktop_App;

import java.awt.*;

public class App {
//    static SynologyServerModel synologyServer = SynologyServerModel.getInstance(); // Initialize the singleton
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