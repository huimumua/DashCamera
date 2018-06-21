package com.askey.dvr.cdr7010.dashcam.core.nmea;

public class NmeaNode {
    public int nodeNumber;
    public String[] dataArray= new String[5];
    public NmeaNode (){
        for (String data:dataArray) {
            data = "";
        }
    }
}
