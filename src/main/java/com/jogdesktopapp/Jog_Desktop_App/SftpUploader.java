package com.jogdesktopapp.Jog_Desktop_App;

import com.jcraft.jsch.*;

import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class SftpUploader {

    // Configurable variables (adjust these for your setup)
    private static final String SFTP_HOST = "192.168.88.186"; // SFTP server URL
    private static final int SFTP_PORT = 22; // SFTP port
    private static final String REMOTE_UPLOAD_DIR = "/JOG 8TB/JOG India"; // Remote directory (removed % encoding)
    private static final String USERNAME = "Synology0822"; 
    private static final String PASSWORD = "InstallSUB2025"; // Store securely

    /**
     * Opens a file picker dialog and uploads the selected file via SFTP.
     */
    public static void pickAndUploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String localFilePath = selectedFile.getAbsolutePath();
            String remoteFilePath = REMOTE_UPLOAD_DIR + "/" + selectedFile.getName(); // Keep original filename

            boolean success = uploadFile(localFilePath, remoteFilePath);
            if (success) {
                JOptionPane.showMessageDialog(null, "✅ File uploaded successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Failed to upload file.");
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

            session = jsch.getSession(USERNAME, SFTP_HOST, SFTP_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no"); // Disable host checking for testing
            System.out.println("🔗 Connecting to SFTP...");
            session.connect(15_000); // Timeout in 15 sec

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(10_000); // Timeout in 10 sec
            System.out.println("✅ SFTP Connected!");

            // Check if the remote directory exists
            try {
                channel.ls(REMOTE_UPLOAD_DIR);
            } catch (SftpException e) {
                System.err.println("❌ Remote directory does not exist or no permission: " + REMOTE_UPLOAD_DIR);
                return false;
            }

            System.out.println("📤 Uploading file: " + localFilePath + " -> " + remoteFilePath);
            
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
    
    /**
     * Downloads a file from the SFTP server.
     */
//    static void downloadFile( ) {
//    	String remoteFilePath = REMOTE_UPLOAD_DIR + "file1.eps";
//        String userHome = System.getProperty("user.home");
//        System.out.println("Initializing connection to Synology NAS..." + userHome);
//        File folder = new File(userHome + "\\Downloads");
//    	String localFilePath = folder.getPath();
//    	System.out.println("Downloaded path is  :  " + localFilePath);
//        Session session = null;
//        ChannelSftp channel = null;
//
//        try {
//        	
//            JSch jsch = new JSch();
//            session = jsch.getSession(USERNAME, SFTP_HOST, SFTP_PORT);
//            session.setPassword(PASSWORD);
//            session.setConfig("StrictHostKeyChecking", "no");
//            System.out.println("🔗 Connecting to SFTP...");
//            session.connect(15_000);
//
//            channel = (ChannelSftp) session.openChannel("sftp");
//            channel.connect(10_000);
//            System.out.println("✅ SFTP Connected!");
//
//            System.out.println("📥 Downloading file: " + remoteFilePath + " -> " + localFilePath);
//
//            try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
//                channel.get(remoteFilePath, fos);
//            }
//
//            System.out.println("✅ Download successful!");
//
//
//        } catch (JSchException e) {
//            System.err.println("❌ SFTP Connection Error: " + e.getMessage());
//        } catch (SftpException e) {
//            System.err.println("❌ SFTP Download Error: " + e.getMessage());
//        } catch (IOException e) {
//            System.err.println("❌ File Write Error: " + e.getMessage());
//        } finally {
//            if (channel != null && channel.isConnected()) {
//                channel.disconnect();
//                System.out.println("🔌 SFTP Channel closed.");
//            }
//            if (session != null && session.isConnected()) {
//                session.disconnect();
//                System.out.println("🔌 SFTP Session closed.");
//            }
//        }
//    }
    
    public static void downloadFile() {
        String remoteFilePath = REMOTE_UPLOAD_DIR + "file1.eps";
        String userHome = System.getProperty("user.home");
        System.out.println("Initializing connection to Synology NAS..." + userHome);
        // Define a safe directory with full access
        File folder = new File(userHome + "\\Documents\\SFTPDownloads");
        // Ensure the directory exists
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // Use a unique filename to avoid conflicts
        String localFilePath = folder.getPath() + "\\file1_" + System.currentTimeMillis() + ".eps";
        System.out.println("Downloaded path is: " + localFilePath);
        Session session = null;
        ChannelSftp channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(USERNAME, SFTP_HOST, SFTP_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println(":link: Connecting to SFTP...");
            session.connect(15_000);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(10_000);
            System.out.println(":white_check_mark: SFTP Connected!");
            System.out.println(":inbox_tray: Downloading file: " + remoteFilePath + " -> " + localFilePath);
            // Use try-with-resources to ensure stream closure
            try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                channel.get(remoteFilePath, fos);
            }
            System.out.println(":white_check_mark: Download successful!");
        } catch (JSchException e) {
            System.err.println(":x: SFTP Connection Error: " + e.getMessage());
        } catch (SftpException e) {
            System.err.println(":x: SFTP Download Error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println(":x: File Write Error: " + e.getMessage());
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
                System.out.println(":electric_plug: SFTP Channel closed.");
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
                System.out.println(":electric_plug: SFTP Session closed.");
            }
        }
    }
}
