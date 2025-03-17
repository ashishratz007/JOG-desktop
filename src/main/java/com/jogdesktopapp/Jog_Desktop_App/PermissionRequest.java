package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
public class PermissionRequest {
    public static void main(String[] args) {
    	createFolderIfNotExits();
    }

    private static void createFolderIfNotExits() { 
        String userHome = System.getProperty("user.home");
        System.out.println("Initializing connection to Synology NAS..." + userHome);
        File folder = new File(userHome + "\\Public\\JOG-Desktop");

        if (!folder.exists()) {
            boolean created = folder.mkdirs(); // Create folder
            if (created) {
                JOptionPane.showMessageDialog(null, "✅ Folder Created.");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Failed to create directory.");
                return;
            }
        }
    }

}
