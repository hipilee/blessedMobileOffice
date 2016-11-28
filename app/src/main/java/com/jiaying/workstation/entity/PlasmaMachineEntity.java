package com.jiaying.workstation.entity;

import java.io.Serializable;

/**
 * 作者：lenovo on 2016/1/20 09:26
 * 邮箱：353510746@qq.com
 * 功能：采浆机实体
 */
public class PlasmaMachineEntity implements Serializable {
    private String nurseID;
    private String nurseName;
    private String nursePic;
    private String locationID;
    private int state;
    private boolean isCheck;

    public PlasmaMachineEntity() {

    }

    public PlasmaMachineEntity(String nurseID, String nurseName, String nursePic, String locationID, int state, boolean isCheck) {
        this.nurseID = nurseID;
        this.nurseName = nurseName;
        this.nursePic = nursePic;
        this.locationID = locationID;
        this.state = state;
        this.isCheck = isCheck;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getNurseID() {
        return nurseID;
    }

    public void setNurseID(String nurseID) {
        this.nurseID = nurseID;
    }

    public String getNurseName() {
        return nurseName;
    }

    public void setNurseName(String nurseName) {
        this.nurseName = nurseName;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getNursePic() {
        return nursePic;
    }

    public void setNursePic(String nursePic) {
        this.nursePic = nursePic;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
