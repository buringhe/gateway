package com.buringhe.gateway.util.jwt;



import java.util.List;


/**
 *  Jwt实体
 * Create by buring  on 2020/7/22
 */

public class JwtModel {

    private String userName;

    private List<String> roleIdList;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getRoleIdList() {
        return roleIdList;
    }

    public void setRoleIdList(List<String> roleIdList) {
        this.roleIdList = roleIdList;
    }
}
