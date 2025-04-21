package com.jogdesktopapp.Jog_Desktop_App;


import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ReprintPendingModel {
  
    int total;
    int page;
    int limit;
    List<ReprintPendingItem> data;

    public ReprintPendingModel(int total, int page, int limit, List<ReprintPendingItem> data) {
      
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.data = data;
    }
    
    public int pageCount() {
        if (total <= 0 || limit <= 0) {
            return 0;
        }
        System.out.println("Total pending items count: " + total);
        int pages = total / limit;
        return (total % limit == 0) ? pages : pages + 1;
    }

    public static ReprintPendingModel fromJson(JSONObject jsonObj) {
       
        int total = jsonObj.getInt("total");
        int page = jsonObj.getInt("page");
        int limit = jsonObj.getInt("limit");
        
        JSONArray dataArray = jsonObj.getJSONArray("data");
        List<ReprintPendingItem> itemList = new ArrayList<>();
        
        for (int i = 0; i < dataArray.length(); i++) {
            itemList.add(ReprintPendingItem.fromJson(dataArray.getJSONObject(i)));
        }
        return new ReprintPendingModel(total, page, limit, itemList);
    }
}

class ReprintPendingItem {
    int reprintStatus;
    int repId;
    int fileId;
    String orderCode;
    String orderName;
    String filePath;
    String synologyPath;
    String fileName;
    int synologyStatus;

    public ReprintPendingItem(int reprintStatus, int repId, int fileId, String orderCode, 
                      String orderName, String filePath, String synologyPath, 
                      String fileName, int synologyStatus) {
        this.reprintStatus = reprintStatus;
        this.repId = repId;
        this.fileId = fileId;
        this.orderCode = orderCode;
        this.orderName = orderName;
        this.filePath = filePath;
        this.synologyPath = synologyPath;
        this.fileName = fileName;
        this.synologyStatus = synologyStatus;
    }

    public static ReprintPendingItem fromJson(JSONObject jsonObj) {
        return new ReprintPendingItem(
            jsonObj.getInt("reprint_status"),
            jsonObj.getInt("rep_id"),
            jsonObj.getInt("file_id"),
            jsonObj.getString("order_code"),
            jsonObj.getString("order_name"),
            jsonObj.getString("file_path"),
            jsonObj.getString("synology_path"),
            jsonObj.getString("file_name"),
            jsonObj.getInt("synology_status")
        );
    }
}