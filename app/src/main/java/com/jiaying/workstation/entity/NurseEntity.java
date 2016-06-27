package com.jiaying.workstation.entity;

import java.io.Serializable;

/**
 * 作者：lenovo on 2016/1/20 00:09
 * 邮箱：353510746@qq.com
 * 功能：护士信息
 */
public class NurseEntity implements Serializable{
    private String name;
    private String id;
    private String sex;
    private String phone;
    private String photo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;

    }
}
