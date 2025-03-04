package com.jogdesktopapp.Jog_Desktop_App;

import com.jcraft.jsch.*;

import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class SftpUploader {

    // Configurable variables (adjust these for your setup)
    private static final String SFTP_HOST = "192.168.88.186"; // SFTP server URL
    private static final int SFTP_PORT = 5000; // SFTP port
    private static final String REMOTE_UPLOAD_DIR = "/jog%208tb/JOG%20India"; // Remote directory to upload files
    private static final String user = "Synology0822"; 
    /**
     * Opens a file picker dialog and uploads the selected file via SFTP.
     */
    static void pickAndUploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String localFilePath = selectedFile.getAbsolutePath();
            String remoteFilePath = REMOTE_UPLOAD_DIR + "/" + selectedFile.getName(); // Use local filename on remote
            
            boolean success = uploadFile(localFilePath, remoteFilePath);
            if (success) {
                JOptionPane.showMessageDialog(null, "File uploaded successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to upload file.");
            }
        }
    }

    /**
     * Uploads a file to the SFTP server with improved error handling and debugging.
     */
    private static boolean uploadFile(String localFilePath, String remoteFilePath) {
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            
            // Enable JSch debugging
            JSch.setLogger(new Logger() {
                public boolean isEnabled(int level) { return true; }
                public void log(int level, String message) { System.out.println("[JSch] " + message); }
            });

            session = jsch.getSession(user, SFTP_HOST, SFTP_PORT);
            String sessionId = "InstallSUB2025";
            session.setPassword(sessionId);
            session.setConfig("StrictHostKeyChecking", "no"); // Disable host checking for testing
            System.out.println("Connecting to SFTP...");
            session.connect(10_000); // Timeout in 10 sec

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(5_000); // Timeout in 5 sec
            System.out.println("SFTP Connected!");

            // Check if the remote directory exists
            try {
                channel.ls(REMOTE_UPLOAD_DIR);
            } catch (SftpException e) {
                System.err.println("❌ Remote directory does not exist or no permission: " + REMOTE_UPLOAD_DIR);
                return false;
            }

            System.out.println("Uploading file: " + localFilePath + " -> " + remoteFilePath);
            channel.put(localFilePath, remoteFilePath, ChannelSftp.OVERWRITE);
            System.out.println("✅ Upload successful!");

            return true;

        } catch (JSchException e) {
            System.err.println("❌ SFTP Connection Error: " + e.getMessage());
        } catch (SftpException e) {
            System.err.println("❌ SFTP Upload Error: " + e.getMessage());
        } finally {
            if (channel != null) {
                channel.disconnect();
                System.out.println("SFTP Channel closed.");
            }
            if (session != null) {
                session.disconnect();
                System.out.println("SFTP Session closed.");
            }
        }
        return false;
    }
}
