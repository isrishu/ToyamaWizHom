package com.toyama.wizhome.hikvision;

public class HikCamera {
    //Number of the camera (1-32, depending on the DVR capabilities)
    private int cameraNumber;
    private boolean isConnected;
    private int playPort;

    public HikCamera(int cameraNumber) {
        this.cameraNumber = cameraNumber;
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    public void setCameraNumber(int cameraNumber) {
        this.cameraNumber = cameraNumber;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean connected) {
        this.isConnected = connected;
    }

    public int getPlayPort() {
        return playPort;
    }

    public void setPlayPort(int playPort) {
        this.playPort = playPort;
    }
}