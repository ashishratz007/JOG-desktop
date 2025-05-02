package com.jogdesktopapp.Jog_Desktop_App;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.SwingWorker;

import org.json.JSONObject;

import com.jogdesktopapp.Jog_Desktop_App.models.NotificationService;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GlobalDataClass {
    private static GlobalDataClass instance;
    private Map<String, Object> dataStore;

    private GlobalDataClass() {
        dataStore = new HashMap<>();
    }
    

    
	// Initialize socket connection
     SocketModel socketModel;
	 SftpUploader sftpClient ; // actions client for upload download and many more
	// to execute the download files for the user.
	 PendingDataModel downloadFiles ;

    public static synchronized GlobalDataClass getInstance() {
        if (instance == null) {
            instance = new GlobalDataClass();
            instance.init();
            
        }
        return instance;
    }

    
   // just binding the function to be called when the instance is created 
   private void init() {
	   reprintDownlaodingData = new ReprintPendingModel(0,0,0, null);
	   reprintDownlaodingData.data = new ArrayList<>(); // Empty list by default
	   reprintPendingData = new ReprintPendingModel(0,0,0, null);
	   reprintPendingData.data = new ArrayList<>(); // Empty list by default
	   redesignDownloadingData = new RedesignPendingModel(0,0,0, null);
	   redesignDownloadingData.data = new ArrayList<>(); // Empty list by default
	   redesignPendingData = new RedesignPendingModel(0,0,0, null);
	   redesignPendingData.data = new ArrayList<>(); // Empty list by default
	   
    }
   
   
    void initilizeData() {
    	downloadFiles= null;
    	sftpClient = null;
    	socketModel = null;
    	setUserRole();
    	downloadFiles = PendingDataModel.getInstance();
    	sftpClient = SftpUploader.getInstance();
    	socketModel = SocketModel.getInstance();
    	System.err.println("Api called for reprint and redesign");
    	pendingAndReprint();
    	socketModel.connectSocket();
    	
    }
    
    void pendingAndReprint(){
    	LocalDate today = LocalDate.now();
        
    	
        // Get the date one year before today
        LocalDate oneYearAgo = today.minusYears(1);

   	    // make a API call when 
        instance.getReprintDownloadingData(1, oneYearAgo, today);// get reprint download
        instance.getReprintPendingData(1, oneYearAgo, today);// get reprint pending
        instance.getReprintCompleteData(1, oneYearAgo, today);// get reprint complete
        instance.getRedesignDownloadingData(1, oneYearAgo, today);// get redesign list download
        instance.getRedesignPendingData(1, oneYearAgo, today);// get redesign pending 
        instance.getRedesignCompleteData(1, oneYearAgo, today);// get redesign complete 
    }
   
    
    
   public String getToken() {
    	JSONObject data = getTokenData();
    	return data.getString("session_token");
    }
   
    // List of listeners

    private final List<UpdateUiListener> uiUpdate = new ArrayList<>();
    /**
     * Adds a pending file get listener.
     */
    public void addUIListener(UpdateUiListener listener) {
    	uiUpdate.add(listener);
    }
    public void removePendingListener(UpdateUiListener listener) {
    	uiUpdate.remove(listener);
    } 
    private void notifyPendingChange() {
        for (UpdateUiListener listener : uiUpdate) {
            listener.onUIChanged();
        }
    }
    
    
   // variables
   NotificationService notification = new NotificationService();
    
    // Reprint data set 
    public ReprintPendingModel reprintDownlaodingData = new ReprintPendingModel(0,0,0, null);  
    public void getReprintDownloadingData(int page,  LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // Call API with dynamic dates
        reprintDownlaodingData = ApiCalls.getReprintPendingList(false,0, 10, page, startDateStr, endDateStr);
        System.err.println("reprint data downloading :  " + reprintDownlaodingData.data.size());
        startDownload();
        // Update reprint count
        AppFrame frame = AppFrame.getInstance();
//        frame.setReprintCount(reprintData.totalCount);
    }  
    
    
    // Reprint data set 
    public ReprintPendingModel reprintPendingData = new ReprintPendingModel(0,0,0, null);  
    public void getReprintPendingData(int page,  LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // Call API with dynamic dates
        reprintPendingData = ApiCalls.getReprintPendingList(true,0, 10, page, startDateStr, endDateStr);
        System.err.println("reprint data pending :  " + reprintPendingData.data.size());
        // Update reprint count
        AppFrame frame = AppFrame.getInstance();
        frame.setReprintCount(reprintPendingData.total);
    }  
    
    
    // Reprint complete data set 
    public ReprintModel reprintCompleteData = new ReprintModel(0, null);  
    public void getReprintCompleteData(int page, LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);
      
 
        // Call API with dynamic dates
        reprintCompleteData = ApiCalls.getReprintList(1, 10, page, startDateStr, endDateStr);
        System.out.println("complete list count : " + reprintCompleteData.data.size());
        
        
    }  
    
    
    // Redesign data set 
    public RedesignPendingModel redesignDownloadingData = new RedesignPendingModel(0,0,0, null);  
    public void getRedesignDownloadingData(int page, LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);
        LocalDate today = LocalDate.now();
        

        // Call API with dynamic dates
        redesignDownloadingData = ApiCalls.getDesignPendingList(false,0, 10, page, startDateStr, endDateStr);
        System.err.println("redsign data downloading :  " + redesignDownloadingData.data.size());
        startDownload();
     // Update redesign count
        AppFrame appFrame = AppFrame.getInstance();
//        appFrame.setRedesignCount(redesignDownloadingData.totalCount);
    }  
    
    // Redesign data set 
    public RedesignPendingModel redesignPendingData = new RedesignPendingModel(0,0,0, null);  
    public void getRedesignPendingData(int page, LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);
        LocalDate today = LocalDate.now();
        

        // Call API with dynamic dates
        redesignPendingData = ApiCalls.getDesignPendingList(true,0, 10, page, startDateStr, endDateStr);
        System.err.println("redsign data pending :  " + redesignPendingData.data.size());
     // Update redesign count
        AppFrame appFrame = AppFrame.getInstance();
        appFrame.setRedesignCount(redesignPendingData.total);
    }  
    
    
    // Redesign completed data set 
    public RedesignModel designCompleteData = new RedesignModel(0, null);  
    public void getRedesignCompleteData(int page, LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // Call API with dynamic dates
        designCompleteData = ApiCalls.getRedesignList(1, 10, page, startDateStr, endDateStr);
    }  
    
   
    
    /// auto download the files to the user system 
    void startDownload(){
    	startDownloadReprint();
    	startDownloadRedesign();
    }
    
    ReprintPendingItem reprintCurrentDownload;
   void startDownloadReprint(){
	   if(reprintCurrentDownload != null) {
           System.out.println("📤 Runnig.......");
			 return;
		 } 
   	EventQueue.invokeLater(() -> {
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					  if (reprintDownlaodingData.data.isEmpty()) {
				            System.out.println("📤 No files to download reprint.");
				            return null;
				        }
					  List<ReprintPendingItem> reprintDownloads = reprintDownlaodingData.data;
				        System.out.println("📤 Starting batch downloading...");

				        while (!reprintDownloads.isEmpty()) {
				        	reprintCurrentDownload = reprintDownloads.get(0);
				        	ReprintPendingItem file = reprintCurrentDownload;
//				        	file.synologyPath + "," + file.file_id + "," + file.exCode +  "," + dateTime.getYear()+ "," + dateTime.getMonthValue()+ "," + dateTime.getDayOfMonth();
				        	String dateStr = formatDate(file.createdOn);
				            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
				        	String fullData = file.synologyPath + "," + file.fileId + "," + file.exCode +  "," + dateTime.getYear()+ "," + dateTime.getMonthValue()+ "," + dateTime.getDayOfMonth(); 
				        	String[] parts = fullData.split(",");
			                String filePath = parts[0];
			                String fileId = parts[1];
			                boolean success = sftpClient.downloadFile(fileId, filePath,false,parts[2],parts[3],parts[4],parts[5],false);
		                       if(success) {
//				            	currentFile = null;
		                    	   ApiCalls.confimDownload(true,reprintCurrentDownload.repId); 
				                System.out.println("✅ Downloaded : " + reprintCurrentDownload.fileName);
				                reprintDownloads.remove(0); // Remove after successful upload
				                reprintDownlaodingData.data.remove(0);
				                continue;
		                       }
		                       else {
		                    	   break;
		                       }
				        }
				        reprintCurrentDownload = null;
				        System.out.println("📤 Download completed.");
					return null;
				}
				@Override
				protected void done() {
					LocalDate today = LocalDate.now();
			        
			    	
			        // Get the date one year before today
			        LocalDate oneYearAgo = today.minusYears(1);
					reprintCurrentDownload = null;
					notifyPendingChange();
					getReprintPendingData(1, oneYearAgo, today);// get reprint pending
				
					System.out.println("Download completed");
				}
			}.execute();

		});
   }
    
   RedesignPendingItem redesignCurrentDownload;
  void startDownloadRedesign(){
	 if(redesignCurrentDownload != null) {
		 return;
	 } 
  	EventQueue.invokeLater(() -> {
			new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					  if (redesignDownloadingData.data.isEmpty()) {
				            System.out.println("📤 No files to download for redesign.");
				            return null;
				        }
					  List<RedesignPendingItem> redesignDownloads = redesignDownloadingData.data;
				        System.out.println("📤 Starting batch downloading...");

				        while (!redesignDownloads.isEmpty()) {
				        	redesignCurrentDownload = redesignDownloads.get(0);
				        	RedesignPendingItem file = redesignCurrentDownload;
//				        	file.synologyPath + "," + file.file_id + "," + file.exCode +  "," + dateTime.getYear()+ "," + dateTime.getMonthValue()+ "," + dateTime.getDayOfMonth();
				        	String dateStr = formatDate(file.created_on);
				            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				            LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
				        	String fullData = file.synologyPath + "," + file.fileId + "," + file.orderCode +  "," + dateTime.getYear()+ "," + dateTime.getMonthValue()+ "," + dateTime.getDayOfMonth(); 
				        	String[] parts = fullData.split(",");
			                String filePath = parts[0];
			                String fileId = parts[1];
			                boolean success = sftpClient.downloadFile(fileId, filePath,false,parts[2],parts[3],parts[4],parts[5],false);
		                       if(success) {
//				            	currentFile = null;
				                System.out.println("✅ Downloaded: " + redesignCurrentDownload.fileName);
				                
				                ApiCalls.confimDownload(false,redesignCurrentDownload.repId);
				                redesignDownloads.remove(0); // Remove after successful upload
				                redesignDownloadingData.data.remove(0);
				                continue;
		                       }
		                       else {
		                    	   break;
		                       }
				        }
				        redesignCurrentDownload = null;
				        System.out.println("📤 Download completed.");
					return null;
				}
				@Override
				protected void done() {
					redesignCurrentDownload = null;
					notifyPendingChange();
					LocalDate today = LocalDate.now();
			        // Get the date one year before today
			        LocalDate oneYearAgo = today.minusYears(1);
			        getRedesignPendingData(1, oneYearAgo, today);// get redesign pending 
					System.out.println("Download completed");
				}
			}.execute();

		});
  }
   
    
   // date format
   String formatDate(String dateString) {
       try {
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
           LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
           dateTime = dateTime.plusHours(7);
           return dateTime.format(formatter);
       } catch (Exception e) {
           System.err.println("Error formatting date: " + dateString + ", error: " + e.getMessage());
           return dateString;
       }
   }
   //==================================================token data========================================================== 
   
   
//    private static final String TOKEN_FILENAME = "token.txt";
    private static final String HIDDEN_TOKEN_FILENAME = ".token.txt";
    
    // Get the appropriate file path based on OS
    private Path getTokenFilePath() {
        String desktop = System.getProperty("user.home") + "/Desktop/";
        String filename = HIDDEN_TOKEN_FILENAME ;
        return Paths.get(desktop + filename);
    }
    
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    // Save token to desktop (optionally hidden)
    public boolean saveTokenToDesktop(String content) {
        Objects.requireNonNull(content, "Token content cannot be null");
        
        try {
            Path filePath = getTokenFilePath();
            
            // Write content to file
            Files.write(filePath, content.getBytes(), 
                       StandardOpenOption.CREATE, 
                       StandardOpenOption.TRUNCATE_EXISTING);
            
            // Set hidden attribute on Windows
            if (isWindows()) {
                try {
                    Files.setAttribute(filePath, "dos:hidden", true);
                } catch (IOException e) {
                    System.err.println("Warning: Could not set hidden attribute: " + e.getMessage());
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving token file: " + e.getMessage());
            return false;
        }
    }
    
    // Create empty token file (optionally hidden)
    public boolean createTokenFile() {
        return saveTokenToDesktop("");
    }
    
    // Read token from desktop (handles both hidden and visible files)
    public String readTokenFromDesktop() {
        try {
            // Try regular filename first
            Path filePath = getTokenFilePath();
            
            // If not found, try hidden filename
            if (!Files.exists(filePath) && !isWindows()) {
                filePath = getTokenFilePath();
            }
            
            if (!Files.exists(filePath)) {
                System.err.println("Token file not found on desktop");
                return null;
            }
            
            if (Files.size(filePath) == 0) {
                System.err.println("Token file is empty");
                return null;
            }
            
            return new String(Files.readAllBytes(filePath));
            
        } catch (IOException e) {
            System.err.println("Error reading token file: " + e.getMessage());
            return null;
        }
    }
    
    // Check if token file exists
    public boolean tokenFileExists() {
        Path regularPath = getTokenFilePath();
        Path hiddenPath = getTokenFilePath();
        
        return Files.exists(regularPath) || (!isWindows() && Files.exists(hiddenPath));
    }
   public JSONObject getTokenData() {
   	String response = GlobalDataClass.getInstance().readTokenFromDesktop();
       if(response == null) {
       	return null;
       }
   	// Parse JSON string
   	JSONObject jsonObject = new JSONObject(response);
   	return jsonObject;
   }
   
   // delete token
   public  boolean deleteTokenFile() {
   	String desktopPath = System.getProperty("user.home") + "/Desktop/.token.txt";
   	Path filePath = Paths.get(desktopPath);
       try {
           if (Files.exists(filePath)) {
               Files.delete(filePath);
           }
           return true;
       } catch (IOException e) {
           System.err.println("Error deleting token file: " + e.getMessage());
           return false;
       }
   }
   
   int userRole = 0;
   void setUserRole(){
	   // for role 
	   JSONObject tokenData = getTokenData();
	   userRole = tokenData.getInt("role");
   }
   
  public String getName () {
	   //fullname
	   JSONObject tokenData = getTokenData();
	   return tokenData.getString("fullname");
   }
   
   
   // role permission 1:Adobe, 2:Reprint, 3: Redesign,4:reprint/redesign
   public boolean isAdobe() {
	   return userRole == 1 || userRole == 4;
   }
   public boolean canReprint() {
	   return userRole == 2 || userRole == 4;
   }
   
   public boolean canRedesign() {
	   return userRole == 3 || userRole == 4;
   }
   
   public boolean admin() {
	   return  userRole == 4;
   }
   
    // Example functions 
    public void putItem(String key, Object value) {
        dataStore.put(key, value);
    }

    public Object getItem(String key) {
        return dataStore.get(key);
    }

    public void removeItem(String key) {
        dataStore.remove(key);
    }

    public boolean containsKey(String key) {
        return dataStore.containsKey(key);
    }
}


//Listener interface for UI updates
interface UpdateUiListener {
 void onUIChanged();
}
