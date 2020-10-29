package com.buringhe.gateway.controller;

import cn.hutool.core.lang.Console;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.buringhe.gateway.api.CommonResult;
import com.buringhe.gateway.model.SysUser;
import com.buringhe.gateway.util.jwt.JwtUtil;
import com.buringhe.gateway.annotation.JWTCheck;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 *  用户管理
 * Create by buring  on 2020/7/22
 */
@RestController
@RequestMapping("/api/user-center/user")
public class UserController {


    // 用户信息接口
    @Value("${org.my.jwt.userInfo-uri}")
    private String userInfoUri;

    /**
     * 获取用户信息信息
     * @param token
     * @return
     * @throws Exception
     */
    @GetMapping("/baseInfo")
    @JWTCheck
    public CommonResult baseInfo(@RequestHeader("Authorization") String token) throws Exception {
        // 1 解析token
        Claims claims = JwtUtil.parseJWT(token);
        String userName = claims.get("username").toString();
        // 2 调用接口验证用户信息是否有效
        HashMap<String, Object> paramReq = new HashMap<>();
        paramReq.put("username", userName);
        String result1= HttpUtil.get(userInfoUri,paramReq);
        CommonResult res = JSONUtil.toBean(result1,CommonResult.class);
        Console.log(result1);
        //2.1 验证用户账号和密码是否有效 并返回用户相关信息
        HashMap<String, Object> paramMap = new HashMap<>();
        if(res.getCode() == 200) {
            paramMap.put("avatar", "https://www.baidu.com.pic.123213");
        }
        return CommonResult.success(paramMap,"查询成功");
    }


}
