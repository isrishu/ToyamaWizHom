package com.toyama.wizhome.hikvision;

/**
 * Created by Srishu Indrakanti on 21-02-2018.
 */

public class HikVisionCamera extends HikCamera {
    public int wizhomCameraId=0,roomId=0;
    public String serialNumber="",lastConnectedIP="",username="",password="",model="";

    public HikVisionCamera(int cameraNumber) {
        super(cameraNumber);
        this.serialNumber=serialNumber;
    }

}
