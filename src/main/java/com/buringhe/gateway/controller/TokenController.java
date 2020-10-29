package com.buringhe.gateway.controller;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.buringhe.gateway.model.SysUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.buringhe.gateway.annotation.JWTCheck;
import com.buringhe.gateway.api.CommonResult;
import com.buringhe.gateway.dto.SysUserLoginParam;
import com.buringhe.gateway.util.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 *  Token管理
 * Create by buring  on 2020/7/22
 */

@RestController
@RequestMapping("/api/user-center/token")
public class TokenController {
    // 用来存储用户信息的对象
    private ObjectMapper objectMapper;

    // token 有效期
    @Value("${org.my.jwt.effective-time}")
    private int effectiveTime;

    // 登录请求接口
    @Value("${org.my.jwt.auth-uri}")
    private String authUri;

    public TokenController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 登陆认证接口
     * @param userLoginParam
     * @return
     * @throws Exception
     */
    @PostMapping("/authorize")
    public CommonResult login(@RequestBody SysUserLoginParam userLoginParam) throws Exception {
        // 1 获取登陆参数
        String username = userLoginParam.getUsername();
        String password = userLoginParam.getPassword();

        // 2 调用接口验证用户账号和密码是否有效
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("username", username); // 用户名
        paramMap.put("password", password); // 密码
        String result1= HttpUtil.get(authUri,paramMap);
        CommonResult res = JSONUtil.toBean(result1,CommonResult.class);
        Console.log(result1);
        // 2.1 判断接口返回结果 若code=200 则创建token 若code!=200 返回错误信息给前台
        if(res.getCode() == 200){
            long effectivTime =  effectiveTime * 1000L;
            String jwt  = JwtUtil.createJWT(IdUtil.simpleUUID(), username, objectMapper.writeValueAsString(username), effectivTime);
            HashMap<String, Object> paramToken = new HashMap<>();
            paramToken.put("token", jwt);
            paramToken.put("expiresIn", effectivTime);
            return CommonResult.success(paramToken,"认证成功");
        }else {
            return CommonResult.failed(res.getMessage());
        }
    }

    /**
     * 退出登录时调用
     * @param token
     * @return
     */
    @GetMapping("/logout")
    @JWTCheck
    public CommonResult logout(@RequestHeader("Authorization") String token)  {
        // 1 解析token
        Claims claims = JwtUtil.parseJWT(token);
        return CommonResult.success(null,"退出登陆成功");
    }

    /**
     * 验证token是否有效
     * @param token
     * @return
     */
    @GetMapping("/verification")
    @JWTCheck
    public CommonResult testJwtCheck(@RequestHeader("Authorization") String token) {
        return CommonResult.success(null,"您是有效地");
    }


}
