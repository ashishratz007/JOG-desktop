package com.jogdesktopapp.Jog_Desktop_App;

import com.jcraft.jsch.*;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class SftpUploader {
    
    private static SftpUploader instance;
    static SftpUploaderStatus currentStatus = SftpUploaderStatus.IDLE;
    
    // List of listeners
    private final List<SftpUploaderListener> listeners = new ArrayList<>();
    
    // LIST of files to be upload 
    public List<UploadFile> pendingUpload = new ArrayList<>(); 
    public UploadFile currentFile = null;
      
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
     * Upload function if there is any pending upload left.
     */
    
    public void uploadFiles() {
        if (pendingUpload.isEmpty()) {
            System.out.println("📤 No files to upload.");
            return;
        }

        System.out.println("📤 Starting batch upload...");

        while (!pendingUpload.isEmpty()) {
            currentFile = pendingUpload.get(0);
            boolean success = uploadFile(currentFile.getPath());

            if (success) {

            	currentFile = null;
                System.out.println("✅ Uploaded: " + currentFile.getPath());
                pendingUpload.remove(0); // Remove after successful upload
            } else {
                System.err.println("❌ Failed to upload: " + currentFile.getPath());
                break; // Stop on failure to avoid infinite loop
            }
        }

        System.out.println("📤 Upload complete.");
    }

    
    public void addFile(UploadFile newFile) {
    	pendingUpload.add(newFile);
    	if(currentFile == null) {
    		 System.out.println("📤 Uplaoding files");
    		uploadFiles();
    	}
    }
    
    public void addFiles(List<UploadFile> newFiles) { 
    	pendingUpload.addAll(newFiles); 
    	if(currentFile == null) {
    		 System.out.println("📤 Uplaoding files");
    		uploadFiles();
    	}
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
//        System.err.println("❌ Status changed to:" + currentStatus);
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
            boolean success = uploadFile(localFilePath);
            notifyStatusChange(SftpUploaderStatus.IDLE);

            JOptionPane.showMessageDialog(null, success ? "✅ File uploaded successfully!" : "❌ Failed to upload file.");
        }
    }

    /**
     * Uploads a file to the SFTP server.
     */
    private boolean uploadFile(String localPath) {
    	 System.out.println("📤 locating file");
    	File selectedFile = new File(localPath);
        String localFilePath = selectedFile.getAbsolutePath();
         String[] paths =  localFilePath.split("/");  
//         String exName = paths[(paths.length - 3)]; 
        String remoteFilePath = REMOTE_UPLOAD_DIR + "/" + selectedFile.getName();
        Session session = null;
        ChannelSftp channel = null;
        System.out.println("📤 uploading action on file");
        notifyStatusChange(SftpUploaderStatus.UPLOADING);
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
            	ApiCalls.confirmUpload(currentFile.getId(), remoteFilePath);
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
        	 notifyStatusChange(SftpUploaderStatus.IDLE);
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



  class UploadFile {
    private String path;
    private String id;
    private String status;

    public UploadFile( String id, String path, String status) {
        this.path = path;
        this.id = id;
        this.status = status;
    }

    // Convert UploadFile object to JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("path", path);
        json.put("id", id);
        json.put("status", status);
        return json;
    }

    // Convert JSON to UploadFile object
    public static UploadFile fromJson(JSONObject json) {
        String path = json.optString("path", "");
        String id = json.optString("id", "");
        String status = json.optString("status", "pending"); // Default status
        return new UploadFile(path, id, status);
    }

    // Method to update the file status
    public void updateStatus(String newStatus) {
        this.status = newStatus;
        System.out.println("🔄 Status updated for File ID " + id + " to: " + newStatus);
    }

    // Getters
    public String getPath() { return path; }
    public String getId() { return id; }
    public String getStatus() { return status; }
}

