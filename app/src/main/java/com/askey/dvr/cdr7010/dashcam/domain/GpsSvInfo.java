package com.askey.dvr.cdr7010.dashcam.domain;

/**
 * 项目名称：MainAPP
 * 类描述：
 * 创建人：
 * 创建时间：2018/4/25 14:50
 * 修改人：skysoft
 * 修改时间：2018/4/25 14:50
 * 修改备注：
 */
public class GpsSvInfo {

    /** Pseudo-random number for the SV. */
    private int     prn;
    /** Signal to noise ratio. */
    private float   snr;
    /** Elevation of SV in degrees. */
    private float   elevation;
    /** Azimuth of SV in degrees. */
    private float   azimuth;

    public int getPrn() {
        return prn;
    }

    public void setPrn(int prn) {
        this.prn = prn;
    }

    public float getSnr() {
        return snr;
    }

    public void setSnr(float snr) {
        this.snr = snr;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }
}
