package com.jogdesktopapp.Jog_Desktop_App;

import com.jcraft.jsch.*;

import javax.swing.*;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

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
     * Upload function if there is any pending upload left.
     */
    
    public void uploadFiles() {
    	EventQueue.invokeLater(() -> {
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					  if (pendingUpload.isEmpty()) {
				            System.out.println("📤 No files to upload.");
				            return null;
				        }

				        System.out.println("📤 Starting batch upload...");

				        while (!pendingUpload.isEmpty()) {
				            currentFile = pendingUpload.get(0);
				            boolean success = uploadFile(currentFile.getPath(), currentFile.getOrderCode());

				            if (success) {

//				            	currentFile = null;
				                System.out.println("✅ Uploaded: " + currentFile.getPath());
				                pendingUpload.remove(0); // Remove after successful upload
				            } else {
				            	File fileToUpload = new File(currentFile.getPath());

				            	// check for the error if the files exits
				                if (!fileToUpload.exists()) {
				                    System.err.println("⚠️ File not found: " + currentFile.getPath());
				                    boolean deleted = ApiCalls.deleteFile(currentFile.getId());
				                    if(deleted) {
				                    	 pendingUpload.remove(0); // Remove missing file from queue
				                    }
				                    continue; // Skip to the next file
				                }
				                else {
				                System.err.println("❌ Failed to upload: " + currentFile.getPath());
				                break; // Stop on failure to avoid infinite loop
				                }
				                }
				        }
				        currentFile = null;
				        System.out.println("📤 Upload complete.");
					return null;
				}
				@Override
				protected void done() {
					System.out.println("Upload complete");
				}
			}.execute();

		});
      
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
    String fileName = selectedFile.getName(); // Extract filename with extension

    // Extract the file name without extension
    String fileBaseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;

    // Construct the imagePath by appending .png to the file base name
    String imagePath = localPath.replace(fileName, fileBaseName + ".png");

    String yearStr = String.valueOf(Year.now().getValue());
    String remoteYearPath = REMOTE_UPLOAD_DIR + "/" + yearStr;
    String remoteUploadPath = remoteYearPath + "/" + uploadFolderName;
    String remoteFilePath = remoteUploadPath + "/" + fileName;

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
        channel.put(localPath, remoteFilePath, ChannelSftp.OVERWRITE);
        System.out.println("✅ File uploaded successfully: " + remoteFilePath);

        System.out.println("✅ Got Local Image Path successfully: " + imagePath);
        System.out.println("✅ Got Local File Path successfully: " + localPath);
        // Convert imagePath to Base64
        String base64Image = encodeFileToBase64(imagePath);

        // Call API with image data
        ApiCalls.confirmUpload(currentFile.getId(), remoteFilePath, base64Image);
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

    // Helper method to convert image to base64
    private String encodeFileToBase64(String filePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            String base64Str =  Base64.getEncoder().encodeToString(fileContent);
            System.err.println("✅ Base64 Generated For File");
            return base64Str;
        } catch (IOException e) {
            System.err.println("❌ Error encoding file to Base64: " + e.getMessage());
            return "";
        }
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
    public void pickAndDownloadFile(String fileId,   String downloadPath, boolean isDesign ,String exCode, String year, String month) {
        downloadFile(fileId, downloadPath,isDesign, exCode, year, month); 
        
    }
    
public void downloadFile(String fileId, String downloadPath,boolean isDesign ,String exCode, String year, String month) {
    notifyStatusChange(SftpUploaderStatus.DOWNLOADING);
    String remoteFilePath = downloadPath;
    String[] dataSplit = remoteFilePath.split("/");
    String fileName = dataSplit[dataSplit.length - 1];
//    String pickedPath = selectDownloadFolder();
//    String localFilePath = pickedPath + "\\" + fileName;

    Session session = null;
    ChannelSftp channel = null;
 // Ensure directory exists
    String storePath = "C:\\Users\\JOG-Graphic\\Desktop\\JOG India Workspace\\download";
    if(isDesign) {
    	storePath = storePath + "//redesign";
    }
    else {
    	storePath = storePath + "//reprint";
    }
    File directory = new File(storePath);
    if (!directory.exists()) {
        boolean created = directory.mkdirs();
        if (!created) {
            System.err.println("❌ Failed to create directories for path: " + storePath);
            notifyStatusChange(SftpUploaderStatus.IDLE);
            return;
        }
    }
    
    try {
    	String localFilePath = storePath + "\\" + fileName;
    	 System.err.println("Path: " + localFilePath);
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

        // Open the downloaded folder in file explorer
        try {
            File downloadedFile = new File(localFilePath);
            if (downloadedFile.exists()) {
                Desktop.getDesktop().open(downloadedFile.getParentFile());
            }
            ApiCalls.confirmDownload(fileId);
        } catch (IOException e) {
            System.err.println("❌ Failed to open download folder: " + e.getMessage());
        }

    } catch (JSchException | SftpException | IOException e) {
        System.err.println("❌ SFTP Download Error: " + e.getMessage());
    } finally {
        notifyStatusChange(SftpUploaderStatus.IDLE);
        if (channel != null) channel.disconnect();
        if (session != null) session.disconnect();
    }
}
    
    // Pick the folder from a directory
    private String selectDownloadFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a folder to save the file");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Only allow directories

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            return selectedFolder.getAbsolutePath(); // Return selected folder path
        }

        return null; // Return null if the user cancels the selection
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
