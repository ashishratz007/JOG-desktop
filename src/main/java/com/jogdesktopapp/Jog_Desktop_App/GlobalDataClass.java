package com.jogdesktopapp.Jog_Desktop_App;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.jogdesktopapp.Jog_Desktop_App.models.NotificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	   reprintPendingData = new ReprintModel(0, null);
	   reprintPendingData.data = new ArrayList<>(); // Empty list by default
	   redesignPendingData = new RedesignModel(0, null);
	   redesignPendingData.data = new ArrayList<>(); // Empty list by default
    }
   
   
    void initilizeData() {
    	
    	downloadFiles= null;
    	sftpClient = null;
    	socketModel = null;
    	downloadFiles = PendingDataModel.getInstance();
    	sftpClient = SftpUploader.getInstance();
    	socketModel = SocketModel.getInstance();
    	socketModel.connectSocket();
    	pendingAndReprint();
    }
    
    void pendingAndReprint(){
    	LocalDate today = LocalDate.now();
        
        // Get the date one year before today
        LocalDate oneYearAgo = today.minusYears(1);

   	// make a API call when 
        instance.getReprintData(1, oneYearAgo, today);// get reprint 
        instance.getReprintCompleteData(1, oneYearAgo, today);// get reprint complete
        instance.getRedesignData(1, oneYearAgo, today);// get redesign list 
        instance.getRedesignCompleteData(1, oneYearAgo, today);// get redesign complete 
    }
   
    String getToken() {
    	JSONObject data = getTokenData();
    	return data.getString("session_token");
    }
   
   // variables
   NotificationService notification = new NotificationService();
    
    // Reprint data set 
    public ReprintModel reprintPendingData = new ReprintModel(0, null);  
    public void getReprintData(int page,  LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // Call API with dynamic dates
        reprintPendingData = ApiCalls.getReprintList(0, 10, page, startDateStr, endDateStr);
        // Update reprint count
        AppFrame frame = AppFrame.getInstance();
        frame.setReprintCount(reprintPendingData.totalCount);
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
    public RedesignModel redesignPendingData = new RedesignModel(0, null);  
    public void getRedesignData(int page, LocalDate startDate,  LocalDate endDate) {
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);
        LocalDate today = LocalDate.now();
        

        // Call API with dynamic dates
        redesignPendingData = ApiCalls.getRedesignList(0, 10, page, startDateStr, endDateStr);
     // Update redesign count
        AppFrame appFrame = AppFrame.getInstance();
        appFrame.setRedesignCount(redesignPendingData.totalCount);
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
    
    
    // save token
    public  boolean saveTokenToDesktop(String content) {
        try {
            // Get the path to the desktop
            String desktopPath = System.getProperty("user.home") + "/Desktop/token.txt";
            Path filePath = Paths.get(desktopPath);
            
            // Write content to file (creates file if it doesn't exist)
            Files.write(filePath, content.getBytes());
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving token file: " + e.getMessage());
            return false;
        }
    }
    
    // read token
    public  String readTokenFromDesktop() {
        try {
            // Get the path to the desktop file
            String desktopPath = System.getProperty("user.home") + "/Desktop/token.txt";
            Path filePath = Paths.get(desktopPath);
            
            // Check if file exists
            if (!Files.exists(filePath)) {
                System.err.println("Token file not found on desktop");
                return null;
            }
            
            // Read all bytes and convert to String
            byte[] fileBytes = Files.readAllBytes(filePath);
            return new String(fileBytes);
            
        } catch (IOException e) {
            System.err.println("Error reading token file: " + e.getMessage());
            return null;
        }
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
    	String desktopPath = System.getProperty("user.home") + "/Desktop/token.txt";
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
