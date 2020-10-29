package com.buringhe.gateway.aop;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.buringhe.gateway.api.CommonResult;
import com.buringhe.gateway.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 *  JWT切面层校验
 * Create by buring  on 2020/7/22
 */
@Component
@Aspect

public class JwtCheckAop {

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.buringhe.gateway.annotation.JWTCheck)")
    private void apiAop() {

    }

    /**
     * 方法执行前后得aop
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("apiAop()")
    public Object aroundApi(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        //获取参数上得所有注解
        Annotation[][] parameterAnnotationArray = method.getParameterAnnotations();
        Object[] args = point.getArgs();

        String token = null;

        /**
         * 找出有 @RequestHeader("Authorization") 的参数，赋值给 token变量
         */
        for (Annotation[] annotations : parameterAnnotationArray) {
            for (Annotation a : annotations) {
                if (a instanceof RequestHeader) {
                    RequestHeader requestHeader = (RequestHeader) a;
                    if ("Authorization".equals(requestHeader.value())) {
                        token = (String) args[ArrayUtils.indexOf(parameterAnnotationArray, annotations)];
                    }
                }
            }
        }


        if (StringUtils.isBlank(token)) {
            //没有token
            return authErro("请登陆");
        } else {
            //有token
            try {
                JwtUtil.checkToken(token, objectMapper);
                Object proceed = point.proceed();
                return proceed;
            } catch (ExpiredJwtException e) {
                //log.error(e.getMessage(), e);
                if (e.getMessage().contains("Allowed clock skew")) {
                    return authErro("认证过期");
                } else {
                    return authErro("认证失败");
                }
            } catch (Exception e) {
                //log.error(e.getMessage(), e);
                return authErro("认证失败");
            }
        }
    }

    /**
     * 认证错误输出
     *
     * @param mess 错误信息
     * @return
     */
    private CommonResult authErro(String mess) {
        return CommonResult.unauthorizedWithMessage(mess);
    }

}

