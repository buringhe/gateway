package com.buringhe.gateway.dto;

import javax.validation.constraints.NotEmpty;

/**
 *  登陆实体，可拓展其他字段
 * Create by buring  on 2020/7/22
 */

public class SysUserLoginParam {

    // 用户名
    private String username;
    // 密码
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
