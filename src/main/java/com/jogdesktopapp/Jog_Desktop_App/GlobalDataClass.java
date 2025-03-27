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
	// make a api call when 
     instance.getReprintData(1);// get reprint 
     instance.getReprintCompleteData(1);// get reprint complete
     instance.getRedesignData(1);// get redesign list 
     instance.getRedesignCompleteData(1);// get redesign complete 
    }
    
   
   
   // variables
   NotificationService notification = new NotificationService();
    
    // Reprint data set 
    public ReprintModel reprintPendingData = new ReprintModel(0, null);  
    public void getReprintData(int page) {
        // Get today's date
        LocalDate today = LocalDate.now();
        
        // Get the date one year before today
        LocalDate oneYearAgo = today.minusYears(1);
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = oneYearAgo.format(formatter);
        String endDate = today.format(formatter);

        // Call API with dynamic dates
        reprintPendingData = ApiCalls.getReprintList(0, 10, page, startDate, endDate);
    }  
    
    // Reprint complete data set 
    public ReprintModel reprintCompleteData = new ReprintModel(0, null);  
    public void getReprintCompleteData(int page) {
        // Get today's date
        LocalDate today = LocalDate.now();
        
        // Get the date one year before today
        LocalDate oneYearAgo = today.minusYears(1);
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = oneYearAgo.format(formatter);
        String endDate = today.format(formatter);

        // Call API with dynamic dates
        reprintCompleteData = ApiCalls.getReprintList(1, 10, page, startDate, endDate);
        System.out.println("complete list count : " + reprintCompleteData.data.size());
        
    }  
    
    
    // Redesign data set 
    public RedesignModel redesignPendingData = new RedesignModel(0, null);  
    public void getRedesignData(int page) {
        // Get today's date
        LocalDate today = LocalDate.now();
        
        // Get the date one year before today
        LocalDate oneYearAgo = today.minusYears(1);
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = oneYearAgo.format(formatter);
        String endDate = today.format(formatter);

        // Call API with dynamic dates
        redesignPendingData = ApiCalls.getRedesignList(0, 10, page, startDate, endDate);
    }  
    
    // Redesign completed data set 
    public RedesignModel designCompleteData = new RedesignModel(0, null);  
    public void getRedesignCompleteData(int page) {
        // Get today's date
        LocalDate today = LocalDate.now();
        
        // Get the date one year before today
        LocalDate oneYearAgo = today.minusYears(1);
        
        // Format the dates as "yyyy-MM-dd"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = oneYearAgo.format(formatter);
        String endDate = today.format(formatter);

        // Call API with dynamic dates
        designCompleteData = ApiCalls.getRedesignList(1, 10, page, startDate, endDate);
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
