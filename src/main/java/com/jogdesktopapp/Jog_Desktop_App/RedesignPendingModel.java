package com.jogdesktopapp.Jog_Desktop_App;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RedesignPendingModel {
    int total;
    int page;
    int limit;
    List<RedesignPendingItem> data;

    public RedesignPendingModel(int total, int page, int limit, List<RedesignPendingItem> data) {
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

    public static RedesignPendingModel fromJson(JSONObject jsonObj) {
        int total = jsonObj.getInt("total");
        int page = jsonObj.getInt("page");
        int limit = jsonObj.getInt("limit");
        
        JSONArray dataArray = jsonObj.getJSONArray("data");
        List<RedesignPendingItem> itemList = new ArrayList<>();
        
        for (int i = 0; i < dataArray.length(); i++) {
            itemList.add(RedesignPendingItem.fromJson(dataArray.getJSONObject(i)));
        }
        return new RedesignPendingModel(total, page, limit, itemList);
    }
}

class RedesignPendingItem {
    int reprintStatus;
    int repId;
    int fileId;
    String orderCode;
    String orderName;
    String filePath;
    String synologyPath;
    String fileName;
    int synologyStatus;
    String note;
    String designerName;
    String created_on;

    public RedesignPendingItem(
    		int reprintStatus,
    		int repId, 
    		int fileId,
    		String orderCode, 
            String orderName,
            String filePath,
            String synologyPath, 
            String fileName,
            int synologyStatus,
            String note,
            String designerName,
            String created_on
    		) {
        this.reprintStatus = reprintStatus;
        this.repId = repId;
        this.fileId = fileId;
        this.orderCode = orderCode;
        this.orderName = orderName;
        this.filePath = filePath;
        this.synologyPath = synologyPath;
        this.fileName = fileName;
        this.synologyStatus = synologyStatus;
        this.note = note;
        this.designerName = designerName;
        this.created_on = created_on;
    }

    public static RedesignPendingItem fromJson(JSONObject jsonObj) {
        return new RedesignPendingItem(
            jsonObj.getInt("redesign_status"),
            jsonObj.getInt("rep_id"),
            jsonObj.getInt("file_id"),
            jsonObj.getString("exCode"),
            jsonObj.getString("orderName"),
            jsonObj.getString("file_path"),
            jsonObj.getString("synologyPath"),
            jsonObj.getString("fileName"),
            jsonObj.getInt("synology_status"),
            jsonObj.getString("note"),
            jsonObj.getString("designerName"),
            jsonObj.getString("created_on")
        );
    }
}