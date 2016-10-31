package org.odk.collect.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by sabbir on 10/20/16.
 */

public class PoliceStation {
    @SerializedName("ক্রমিক নং")
    private String serialNo;
    @SerializedName("কর্মকর্তা/প্রতিষ্ঠান")
    private String position;
    @SerializedName("ঠিকানা")
    private String thana;
    @SerializedName("district")
    private String district;
    @SerializedName("ফোন/মোবাইল নম্বর")
    private String mobileNo;

    List<PoliceStation> stationList;

    public PoliceStation(){}

    public PoliceStation(String serialNo, String position, String thana, String mobileNo) {
        this.serialNo = serialNo;
        this.position = position;
        this.thana = thana;
        this.district = district;
        this.mobileNo = mobileNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getThana() {
        return thana;
    }

    public void setThana(String thana) {
        this.thana = thana;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public List<PoliceStation> getStationList() {
        return stationList;
    }

    public void setStationList(List<PoliceStation> stationList) {
        this.stationList = stationList;
    }
}
