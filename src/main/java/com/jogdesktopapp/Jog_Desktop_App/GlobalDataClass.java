package com.jogdesktopapp.Jog_Desktop_App;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogdesktopapp.Jog_Desktop_App.models.NotificationService;

public class GlobalDataClass {
    private static GlobalDataClass instance;
    private Map<String, Object> dataStore;

    private GlobalDataClass() {
        dataStore = new HashMap<>();
    }

    public static synchronized GlobalDataClass getInstance() {
        if (instance == null) {
            instance = new GlobalDataClass();
            instance.init();
            
        }
        return instance;
    }

    
   // just binding the function to be called when the instance is created 
   private void init() {
     LocalDate today = LocalDate.now();
     
     // Get the date one year before today
     LocalDate oneYearAgo = today.minusYears(1);

	// make a api call when 
     instance.getReprintData(1, oneYearAgo, today);// get reprint 
     instance.getReprintCompleteData(1, oneYearAgo, today);// get reprint complete
     instance.getRedesignData(1, oneYearAgo, today);// get redesign list 
     instance.getRedesignCompleteData(1, oneYearAgo, today);// get redesign complete 
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
