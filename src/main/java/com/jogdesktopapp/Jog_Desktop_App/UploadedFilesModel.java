package com.jogdesktopapp.Jog_Desktop_App;


import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public  class UploadedFilesModel {
    int totalCount; 
    List<UploadedFile> data;

    public UploadedFilesModel(int totalCount, List<UploadedFile> data) {
        this.totalCount = totalCount;
        this.data = data;
    }
    
    public int pageCount() {
        if (totalCount <= 0) {
            return 0;
        }
        System.out.println("pending list count : " + totalCount);
        int pages = totalCount / 10;
        return (totalCount % 10 == 0) ? pages  : pages + 1;
    }

    public static UploadedFilesModel fromJson(JSONObject jsonObj) {
        int totalCount = jsonObj.getInt("totalCount");
        JSONArray dataArray = jsonObj.getJSONArray("data");
        List<UploadedFile> itemList = new ArrayList<>();
        
        for (int i = 0; i < dataArray.length(); i++) {
            itemList.add(UploadedFile.fromJson(dataArray.getJSONObject(i)));
        }
        return new UploadedFilesModel(totalCount, itemList);
    }
}

class UploadedFile {
    String fileName;
    String orderName;
    String exCode;
    String downloadedPath; 
    String synologyPath;
    String created_on;

    public UploadedFile(String fileName, String orderName, String exCode, String synologyPath,String downloadedPath, String created_on) {
       
        this.fileName = fileName;
        this.orderName = orderName;
        this.downloadedPath = downloadedPath;
        this.synologyPath = synologyPath;
        this.exCode = exCode;
        this.created_on = created_on;
    }

    public static UploadedFile fromJson(JSONObject jsonObj) {
        return new UploadedFile(
            jsonObj.getString("fileName"),
            jsonObj.getString("orderName"),
            jsonObj.getString("exCode"),
            jsonObj.getString("synology_path"),
            jsonObj.optString("downloadedPath", ""),
            jsonObj.optString("created_on", "")
        );
    }
}