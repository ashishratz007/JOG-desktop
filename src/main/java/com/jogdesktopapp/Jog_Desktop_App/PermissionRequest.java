package com.jogdesktopapp.Jog_Desktop_App;

import javax.swing.*;
import java.io.File;

public class PermissionRequest {
    public static void main(String[] args) {
        int response = JOptionPane.showConfirmDialog(
                null,
                "This application needs write access to the folder.\nGrant permission?",
                "Permission Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            grantWritePermission();
        } else {
            JOptionPane.showMessageDialog(null, "⚠ Write permission denied. The app may not work correctly.");
        }
    }

    private static void grantWritePermission() {
        File folder = new File("C:\\JOG-Graphic\\Desktop\\JOG India Workspace\\Files");

        if (!folder.exists()) {
            folder.mkdirs(); // Create folder if it doesn't exist
        }

        boolean success = folder.setWritable(true, false); // Grant write permission
        if (success) {
            JOptionPane.showMessageDialog(null, "✅ Write permission granted!");
        } else {
            JOptionPane.showMessageDialog(null, "❌ Failed to enable write permission. Run as Administrator.");
        }
    }
}
