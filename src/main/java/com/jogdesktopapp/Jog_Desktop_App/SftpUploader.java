package com.jogdesktopapp.Jog_Desktop_App;

import com.jcraft.jsch.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SftpUploader {
    
    private static SftpUploader instance;
    private static SftpUploaderStatus currentStatus = SftpUploaderStatus.IDLE;
    
    // List of listeners
    private final List<SftpUploaderListener> listeners = new ArrayList<>();

    // SFTP Configuration
    final String SFTP_HOST = "192.168.88.186";
    private final int SFTP_PORT = 22;
    private final String REMOTE_UPLOAD_DIR = "/JOG 8TB/JOG India";
    private final String USERNAME = "Synology0822";
    private final String PASSWORD = "InstallSUB2025"; // Store securely

    // Singleton instance
    public static SftpUploader getInstance() {
        if (instance == null) {
            instance = new SftpUploader();
        }
        return instance;
    }

    /**
     * Adds a listener to observe status changes.
     */
    public void addListener(SftpUploaderListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     */
    public void removeListener(SftpUploaderListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners about a status update.
     */
    private void notifyStatusChange(SftpUploaderStatus newStatus) {
        currentStatus = newStatus;
        for (SftpUploaderListener listener : listeners) {
            listener.onStatusChanged(newStatus);
        }
    }

    /**
     * Opens a file picker dialog and uploads the selected file via SFTP.
     */
    public void pickAndUploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String localFilePath = selectedFile.getAbsolutePath();
            String remoteFilePath = REMOTE_UPLOAD_DIR + "/" + selectedFile.getName();

            notifyStatusChange(SftpUploaderStatus.UPLOADING);
            boolean success = uploadFile(localFilePath, remoteFilePath);
            notifyStatusChange(SftpUploaderStatus.IDLE);

            JOptionPane.showMessageDialog(null, success ? "✅ File uploaded successfully!" : "❌ Failed to upload file.");
        }
    }

    /**
     * Uploads a file to the SFTP server.
     */
    private boolean uploadFile(String localFilePath, String remoteFilePath) {
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(USERNAME, SFTP_HOST, SFTP_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15_000);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(10_000);

            // Check remote directory
            try {
                channel.ls(REMOTE_UPLOAD_DIR);
            } catch (SftpException e) {
            	notifyStatusChange(SftpUploaderStatus.IDLE);
                System.err.println("❌ Remote directory does not exist or no permission.");
                return false;
            }

            channel.put(localFilePath, remoteFilePath, ChannelSftp.OVERWRITE);
            return true;

        } catch (JSchException | SftpException e) {
            System.err.println("❌ SFTP Error: " + e.getMessage());
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
        return false;
    }

    /**
     * Downloads a file from the SFTP server.
     */
    public void downloadFile() {
    	 notifyStatusChange(SftpUploaderStatus.DOWNLOADING);
        
        String remoteFilePath = REMOTE_UPLOAD_DIR + "/file1.eps";
        String[] dataSplit = remoteFilePath.split("/");
        String fileName = dataSplit[dataSplit.length - 1];
        String userHome = System.getProperty("user.home");
        String localFilePath = userHome + "\\Public\\JOG-Desktop\\" + fileName;

        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(USERNAME, SFTP_HOST, SFTP_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15_000);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(10_000);

            notifyStatusChange(SftpUploaderStatus.DOWNLOADING);

            try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                channel.get(remoteFilePath, fos);
            }

            notifyStatusChange(SftpUploaderStatus.IDLE);

        } catch (JSchException | SftpException | IOException e) {
            System.err.println("❌ SFTP Download Error: " + e.getMessage());
        } finally {
        	notifyStatusChange(SftpUploaderStatus.IDLE);
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}

// Enum to track the status of operations
enum SftpUploaderStatus {
    IDLE, UPLOADING, DOWNLOADING
}

// Listener interface for UI updates
interface SftpUploaderListener {
    void onStatusChanged(SftpUploaderStatus newStatus);
}
