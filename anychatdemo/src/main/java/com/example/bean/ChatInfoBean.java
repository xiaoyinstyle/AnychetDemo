package com.example.bean;

/**
 * Created by Chne on 2017/8/2.
 */

public class ChatInfoBean {
    private int id;
    private String name;

    public ChatInfoBean() {

    }

    public ChatInfoBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public ChatInfoBean(int id, String name, boolean choose) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
