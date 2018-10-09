package com.pro.sau.fsync.model;

import java.util.List;

public class ImeiModel {


    /**
     * App_Status : started
     * data : ["777888999444555","111222333444555","866257033789669"]
     */

    private String App_Status;
    private List<String> data;

    public String getApp_Status() {
        return App_Status;
    }

    public void setApp_Status(String App_Status) {
        this.App_Status = App_Status;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
