package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import java.io.File;

public class PermissionRequest {
    public static void main(String[] args) {
    	createFolderIfNotExits();
    }

    private static void createFolderIfNotExits() { 
        String userHome = System.getProperty("user.home");
        System.out.println("Initializing connection to Synology NAS..." + userHome);
        File folder = new File(userHome + "\\Documents\\JOGDesktop");

        if (!folder.exists()) {
            boolean created = folder.mkdirs(); // Create folder
            if (created) {
                JOptionPane.showMessageDialog(null, "✅ Folder Created.");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Failed to create directory.");
                return;
            }
        }

        boolean success = folder.setWritable(true, true); // Grant write permission
        if (success) {
            JOptionPane.showMessageDialog(null, "✅ Write permission granted!");
        } else {
            JOptionPane.showMessageDialog(null, "❌ Failed to grant permission.");
        }
    }

}
