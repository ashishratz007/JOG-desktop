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


public  class ReprintModel {
    int totalCount;
    List<ReprintItem> data;

    public ReprintModel(int totalCount, List<ReprintItem> data) {
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

    public static ReprintModel fromJson(JSONObject jsonObj) {
        int totalCount = jsonObj.getInt("total_count");
        JSONArray dataArray = jsonObj.getJSONArray("data");
        List<ReprintItem> itemList = new ArrayList<>();
        
        for (int i = 0; i < dataArray.length(); i++) {
            itemList.add(ReprintItem.fromJson(dataArray.getJSONObject(i)));
        }
        return new ReprintModel(totalCount, itemList);
    }
}

class ReprintItem {
    int reprintId;
    String fileName;
    String orderName;
    String exCode;
    String printerName;
    String synologyPath;
    String created_on;
    String note;

    public ReprintItem(int reprintId, String fileName, String orderName, String exCode, String synologyPath,String printerName, String note, String created_on) {
        this.reprintId = reprintId;
        this.fileName = fileName;
        this.orderName = orderName;
        this.printerName = printerName;
        this.synologyPath = synologyPath;
        this.exCode = exCode;
        this.note = note;
        this.created_on = created_on;
    }

    public static ReprintItem fromJson(JSONObject jsonObj) {
        return new ReprintItem(
            jsonObj.getInt("reprint_id"),
            jsonObj.getString("fileName"),
            jsonObj.getString("orderName"),
            jsonObj.getString("exCode"),
            jsonObj.getString("synology_path"),
            jsonObj.optString("printerName", ""),
            jsonObj.optString("note", ""),
            jsonObj.optString("created_on", "")
        );
    }
}