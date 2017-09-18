package com.example.bean;

/**
 * Created by Chne on 2017/8/3.
 */

public class RoomBean {
    private int roomId;
    private int[] users;

    public RoomBean() {

    }

    public RoomBean(int roomId, int[] users) {
        this.roomId = roomId;
        this.users = users;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int[] getUsers() {
        return users;
    }

    public void setUsers(int[] users) {
        this.users = users;
    }
}
