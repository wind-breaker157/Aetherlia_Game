package com.aetherlia.battle;

public class CaptureResult {

    private String message;
    private boolean success;
    private int shakes;

    public CaptureResult(
            String message,
            boolean success,
            int shakes) {

        this.message = message;
        this.success = success;
        this.shakes = shakes;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getShakes() {
        return shakes;
    }
}