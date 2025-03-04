package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import java.io.File;

public class PermissionRequest {
    public static void main(String[] args) {
    	createFolderIfNotExits();
    }

    private static void createFolderIfNotExits() { 
        File folder = new File("C:\\Program Files\\JOGDesktop");

        if (!folder.exists()) {
            folder.mkdirs(); // Create folder if it doesn't exist
            JOptionPane.showMessageDialog(null, "✅ Folder Created.");
        }

        boolean success = folder.setWritable(true, true); // Grant write permission
        if (success) {
            JOptionPane.showMessageDialog(null, "✅ Write permission granted!");
        } else {
            JOptionPane.showMessageDialog(null, "❌ Failed to create folder");
        }
    }
}
