package com.jogdesktopapp.Jog_Desktop_App;

import com.jcraft.jsch.*;

import javax.swing.*;
import java.io.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class SftpUploader {
    
    private static SftpUploader instance;
    static SftpUploaderStatus currentStatus = SftpUploaderStatus.IDLE;
    
    // List of listeners
    private final List<SftpUploaderListener> statusListeners = new ArrayList<>();
    
    private final List<SftpUploaderListener> pendingFileListeners = new ArrayList<>();
    
    
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
     * Get pending files if exits 
     */
    
    void getPendingFiles() {
    	 pendingUpload =  ApiCalls.getPendingFiles();
    	 uploadFiles();
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
            boolean success = uploadFile(currentFile.getPath(), currentFile.getOrderCode());

            if (success) {

//            	currentFile = null;
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
     * Adds a pending file get listener.
     */
    public void addPendingListener(SftpUploaderListener listener) {
    	pendingFileListeners.add(listener);
    }
    public void removePendingListener(SftpUploaderListener listener) {
        statusListeners.remove(listener);
    } 
    private void notifyPendingChange(SftpUploaderStatus newStatus) {
        currentStatus = newStatus;
        for (SftpUploaderListener listener : statusListeners) {
            listener.onPendingChanged();
        }
    }
    /**
     * Adds a status listener to observe status changes.
     */
    public void addStatusListener(SftpUploaderListener listener) {
        statusListeners.add(listener);
    }

    /**
     * Removes a status listener.
     */
    public void removeStatusListener(SftpUploaderListener listener) {
        statusListeners.remove(listener);
    }

    /**
     * Notifies all registered listeners about a status update.
     */
    private void notifyStatusChange(SftpUploaderStatus newStatus) {
        currentStatus = newStatus;
//        System.err.println("❌ Status changed to:" + currentStatus);
        for (SftpUploaderListener listener : statusListeners) {
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
            boolean success = uploadFile(localFilePath, "Dummy Data"); 
            notifyStatusChange(SftpUploaderStatus.IDLE);

            JOptionPane.showMessageDialog(null, success ? "✅ File uploaded successfully!" : "❌ Failed to upload file.");
        }
    }

    /**
     * Uploads a file to the SFTP server.
     * Also [uploadFolderName] is to upload the file to the folder to make it as a separate
     * Also get year to store the data as year wise upload will happen as year wise
     */
    private boolean uploadFile(String localPath, String uploadFolderName) {
        System.out.println("📤 Locating file");
        File selectedFile = new File(localPath);
        String localFilePath = selectedFile.getAbsolutePath();
        String yearStr = String.valueOf(Year.now().getValue());
        String remoteYearPath = REMOTE_UPLOAD_DIR + "/" + yearStr;
        String remoteUploadPath = remoteYearPath + "/" + uploadFolderName;
        String remoteFilePath = remoteUploadPath + "/" + selectedFile.getName();

        Session session = null;
        ChannelSftp channel = null;
        System.out.println("📤 Uploading action on file");
        notifyStatusChange(SftpUploaderStatus.UPLOADING);

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(USERNAME, SFTP_HOST, SFTP_PORT);
            session.setPassword(PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(15_000);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(10_000);

            // Ensure the year folder exists
            createRemoteFolderIfNotExists(channel, remoteYearPath);
            // Ensure the upload folder exists inside the year folder
            createRemoteFolderIfNotExists(channel, remoteUploadPath);

            // Upload the file
            channel.put(localFilePath, remoteFilePath, ChannelSftp.OVERWRITE);
            System.out.println("✅ File uploaded successfully: " + remoteFilePath);
            ApiCalls.confirmUpload(currentFile.getId(), remoteFilePath);
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
     create the folder if not exits in the synology server 
     */
    private void createRemoteFolderIfNotExists(ChannelSftp channel, String remotePath) {
        try {
            channel.ls(remotePath); // Check if the directory exists
        } catch (SftpException e) {
            System.out.println("📂 Creating missing directory: " + remotePath);
            try {
                channel.mkdir(remotePath);
            } catch (SftpException ex) {
                System.err.println("❌ Failed to create directory: " + remotePath);
            }
        }
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
    void onPendingChanged();
}


class UploadFile {
    private String path;
    private String id;
    private String status;
    private String orderCode;

    public UploadFile(String id, String path, String status, String orderCode) {
        this.id = id;
        this.path = path;
        this.status = status;
        this.orderCode = orderCode;
    }

    // Convert UploadFile object to JSON
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("file_path", path);
        json.put("status", status);
        json.put("order_code", orderCode);
        return json;
    }

    // Convert JSON to UploadFile object
    public static UploadFile fromJson(JSONObject json) {
        String id = json.optString("id", "");  
        String path = json.optString("file_path", "");  
        String status = json.optString("status", "pending"); // Default status
        String orderCode = json.optString("order_code", "");  
        return new UploadFile(id, path, status, orderCode);
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
    public String getOrderCode() { return orderCode; }
}
