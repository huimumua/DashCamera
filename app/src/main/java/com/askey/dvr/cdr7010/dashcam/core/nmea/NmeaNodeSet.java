package com.askey.dvr.cdr7010.dashcam.core.nmea;

public class NmeaNodeSet {
    public NmeaNode[] nmeaNodes = new NmeaNode[2];
    public long keyNumber;
    public NmeaNodeSet() {
        for (NmeaNode nmeaNode:nmeaNodes) {
            nmeaNode = new NmeaNode();
        }
    }

}
