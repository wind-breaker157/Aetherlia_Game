package com.aetherlia.game;

public class MovementResult {

    private int x;
    private int y;
    private String facing;
    private boolean blocked;
    private String locationCode;


    public MovementResult() {
    }


    public MovementResult(
            int x,
            int y,
            String facing,
            boolean blocked,
            String locationCode) {

        this.x = x;
        this.y = y;
        this.facing = facing;
        this.blocked = blocked;
        this.locationCode = locationCode;
    }


    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }


    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }


    public String getFacing() {
        return facing;
    }

    public void setFacing(String facing) {
        this.facing = facing;
    }


    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }


    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }
}