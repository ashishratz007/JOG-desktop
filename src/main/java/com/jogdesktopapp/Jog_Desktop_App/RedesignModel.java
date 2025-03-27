package com.jogdesktopapp.Jog_Desktop_App;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RedesignModel {
    int totalCount;
    List<RedesignItem> data;

    public RedesignModel(int totalCount, List<RedesignItem> data) {
        this.totalCount = totalCount;
        this.data = data;
    }
    
    public int pageCount() {
        if (totalCount <= 0) {
            return 0;  // No items means no pages
        }
        
        return (totalCount) / 10;
     
    }
    public static RedesignModel fromJson(JSONObject jsonObj) {
        int totalCount = jsonObj.getInt("total_count");
        JSONArray dataArray = jsonObj.getJSONArray("data");
        List<RedesignItem> itemList = new ArrayList<>();

        for (int i = 0; i < dataArray.length(); i++) {
            itemList.add(RedesignItem.fromJson(dataArray.getJSONObject(i)));
        }
        return new RedesignModel(totalCount, itemList);
    }
}

class RedesignItem {
    int redesignId;
    String orderName;
    String fileName;
    String exCode;
    String note;
    String synologyPath;
    String designerName;

    public RedesignItem(int redesignId, String orderName, String fileName, String exCode, String note, String synologyPath, String designerName) {
        this.redesignId = redesignId;
        this.orderName = orderName;
        this.fileName = fileName;
        this.exCode = exCode;
        this.note = note;
        this.synologyPath = synologyPath;
        this.designerName = designerName;
    }

    public static RedesignItem fromJson(JSONObject jsonObj) {
        return new RedesignItem(
            jsonObj.getInt("redesign_id"),
            jsonObj.getString("orderName"),
            jsonObj.getString("fileName"),
            jsonObj.getString("exCode"),
            jsonObj.optString("note", ""),
            jsonObj.optString("synology_path", ""),
            jsonObj.optString("designerName", "")
        );
    }
}
