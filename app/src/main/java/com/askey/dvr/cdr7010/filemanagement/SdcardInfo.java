package com.askey.dvr.cdr7010.filemanagement;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 项目名称：filemanagement
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/4/17 16:39
 * 修改人：skysoft
 * 修改时间：2018/4/17 16:39
 * 修改备注：
 */
public class SdcardInfo implements Parcelable {

    private String totalSize;
    private String normalSize;
    private String normal1CurrentSize;
    private String normal2CurrentSize;
    private String eventSize;
    private String eventCurrentSize;
    private String parkingSize;
    private String parkingCurrentSize;
    private String pictureSize;
    private String pictureCurrentSize;

    public SdcardInfo() { }

    protected SdcardInfo(Parcel in) {
        totalSize = in.readString();
        normalSize = in.readString();
        normal1CurrentSize = in.readString();
        normal2CurrentSize = in.readString();
        eventSize = in.readString();
        eventCurrentSize = in.readString();
        parkingSize = in.readString();
        parkingCurrentSize = in.readString();
        pictureSize = in.readString();
        pictureCurrentSize = in.readString();
    }

    public static final Creator<SdcardInfo> CREATOR = new Creator<SdcardInfo>() {
        @Override
        public SdcardInfo createFromParcel(Parcel in) {
            return new SdcardInfo(in);
        }

        @Override
        public SdcardInfo[] newArray(int size) {
            return new SdcardInfo[size];
        }
    };

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getNormalSize() {
        return normalSize;
    }

    public void setNormalSize(String normalSize) {
        this.normalSize = normalSize;
    }

    public String getNormal1CurrentSize() {
        return normal1CurrentSize;
    }

    public void setNormal1CurrentSize(String normal1CurrentSize) {
        this.normal1CurrentSize = normal1CurrentSize;
    }

    public String getNormal2CurrentSize() {
        return normal2CurrentSize;
    }

    public void setNormal2CurrentSize(String normal2CurrentSize) {
        this.normal2CurrentSize = normal2CurrentSize;
    }

    public String getEventSize() {
        return eventSize;
    }

    public void setEventSize(String eventSize) {
        this.eventSize = eventSize;
    }

    public String getEventCurrentSize() {
        return eventCurrentSize;
    }

    public void setEventCurrentSize(String eventCurrentSize) {
        this.eventCurrentSize = eventCurrentSize;
    }

    public String getParkingSize() {
        return parkingSize;
    }

    public void setParkingSize(String parkingSize) {
        this.parkingSize = parkingSize;
    }

    public String getParkingCurrentSize() {
        return parkingCurrentSize;
    }

    public void setParkingCurrentSize(String parkingCurrentSize) {
        this.parkingCurrentSize = parkingCurrentSize;
    }

    public String getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(String pictureSize) {
        this.pictureSize = pictureSize;
    }

    public String getPictureCurrentSize() {
        return pictureCurrentSize;
    }

    public void setPictureCurrentSize(String pictureCurrentSize) {
        this.pictureCurrentSize = pictureCurrentSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(totalSize);
        dest.writeString(normalSize);
        dest.writeString(normal1CurrentSize);
        dest.writeString(normal2CurrentSize);
        dest.writeString(eventSize);
        dest.writeString(eventCurrentSize);
        dest.writeString(parkingSize);
        dest.writeString(parkingCurrentSize);
        dest.writeString(pictureSize);
        dest.writeString(pictureCurrentSize);

    }
}
