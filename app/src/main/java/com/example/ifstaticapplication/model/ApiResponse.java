package com.example.ifstaticapplication.model;

import java.util.List;

public class ApiResponse {
    private String status;
    private String code;
    private List<Restaurant> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Restaurant> getData() {
        return data;
    }

    public void setData(List<Restaurant> data) {
        this.data = data;
    }
}

