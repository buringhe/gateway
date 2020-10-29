package com.buringhe.gateway.dto;



/**
 *  返回结果
 * Create by buring  on 2020/7/22
 */

public class Resp<T> {

    private int code;

    private String mass;

    private T data;

    public Resp(int scUnauthorized, T mess, T mess1) {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
