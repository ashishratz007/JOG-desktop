package com.jogdesktopapp.Jog_Desktop_App;

import java.util.ArrayList;
import java.util.List;

public class PendingDataModel {
	// to make work easier to filter you data
	static GlobalDataClass globalData = GlobalDataClass.getInstance();
		
    private static volatile PendingDataModel instance;

   
    private volatile boolean downloadReprintRunning = true;
    private volatile boolean downloadRedeignRunning = true;


    public static PendingDataModel getInstance() {
        if (instance == null) {
            synchronized (PendingDataModel.class) {
                if (instance == null) {
                    instance = new PendingDataModel();
                }
            }
        }
        return instance;
    }

    // Simulated processing method: download reprint data
    private void downloadPendingReprintData() {
        new Thread(() -> {
            while (downloadReprintRunning) {
            	
                synchronized (globalData.reprintPendingData) {
                	 List<ReprintItem> pendingList = globalData.reprintPendingData.data;
                    if (!pendingList.isEmpty()) {
                    	ReprintItem item = pendingList.getFirst();

                        try {
                        	//C:\Users\JOG-Graphic\Desktop\JOG India Workspace\download
                        	pendingList.remove(0);
                        	Thread.sleep(1000); // 1 second per item
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Reprint thread interrupted.");
                        }
                    } else {
                        System.out.println("All reprint pending data processed.");
                        downloadReprintRunning = false;
                    }
                }
            }
        }).start();
    }

    public void startReprintDownload() {
        downloadReprintRunning = true;
        downloadPendingReprintData();
    }

    // Simulated processing method: download redesign data
    private void downloadPendingRedesignData() {
        new Thread(() -> {
            while (downloadRedeignRunning) {
            	 List<RedesignItem> pendingList = globalData.redesignPendingData.data;
                synchronized (pendingList) {
                    if (!pendingList.isEmpty()) {
//                        String item = pendingList.remove(0);
//                        System.out.println("Processing Redesign: " + item);

                        try {
                            Thread.sleep(1000); // 1 second per item
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Redesign thread interrupted.");
                        }
                    } else {
                        System.out.println("All redesign pending data processed.");
                        downloadRedeignRunning = false;
                    }
                }
            }
        }).start();
    }

    public void startRedesignDownload() {
        downloadRedeignRunning = true;
        downloadPendingRedesignData();
    }
}
