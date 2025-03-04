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
     * Uploads a file to the SFTP server.
     */
    private static boolean uploadFile(String localFilePath, String remoteFilePath) {
        Session session = null;
        ChannelSftp channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession("Synology0822", SFTP_HOST, SFTP_PORT);
            String sessionId = App.synologyServer.sessionId.toString();
            session.setPassword(sessionId);
            session.setConfig("StrictHostKeyChecking", "no"); // Disable host check (for testing)
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.put(localFilePath, remoteFilePath);
            return true;
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}
