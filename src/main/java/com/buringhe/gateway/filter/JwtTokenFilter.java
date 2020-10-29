package com.buringhe.gateway.filter;


import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.buringhe.gateway.api.CommonResult;
import com.buringhe.gateway.config.IgnoreUrlsConfig;
import com.buringhe.gateway.service.AuthConstant;
import com.buringhe.gateway.util.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;


import io.netty.buffer.ByteBufAllocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.server.reactive.ServerHttpRequest;


/**
 *  token过滤
 * Create by buring  on 2020/7/22
 */

@Component
public class JwtTokenFilter implements GlobalFilter, Ordered {

    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;

    private ObjectMapper objectMapper;

    public JwtTokenFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    /**
     * 过滤器
     *
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();
        Console.log("请求url="+url);
        List<String> ignoreUrls = ignoreUrlsConfig.getUrls();
        ServerHttpRequest request = exchange.getRequest();
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, url)) {
                request = exchange.getRequest().mutate().header(AuthConstant.JWT_TOKEN_HEADER, "").build();
                exchange = exchange.mutate().request(request).build();
                Console.log("url="+url+"在被忽略白名单中");
                return chain.filter(exchange);
            }
        }
        // 2 获取token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        ServerHttpResponse resp = exchange.getResponse();

        if (StrUtil.isBlank(token)) {
            // 2.1 没有Token
            return authErro(resp, "请登陆");
        } else {
            // 2.2 有token
            try {
                JwtUtil.checkToken(token, objectMapper);
                return chain.filter(exchange);
            } catch (ExpiredJwtException e) {
                //log.error(e.getMessage(), e);
                if (e.getMessage().contains("Allowed clock skew")) {
                    return authErro(resp, "认证过期");
                } else {
                    return authErro(resp, "认证失败");
                }
            } catch (Exception e) {
                //log.error(e.getMessage(), e);
                return authErro(resp, "认证失败");
            }
        }
    }
    /**
     * 从Flux<DataBuffer>中获取字符串的方法
     * @return 请求体
     */
    private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();

        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        //获取request body
        return bodyRef.get();
    }
    /**
     * 字符串转DataBuffer
     * @param value
     * @return
     */
    private DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }


    /**
     * 认证错误输出
     *
     * @param resp 响应对象
     * @param mess 错误信息
     * @return
     */
    private Mono<Void> authErro(ServerHttpResponse resp, String mess) {
        resp.setStatusCode(HttpStatus.OK);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String returnStr = "";
        try {
            returnStr = objectMapper.writeValueAsString(CommonResult.unauthorizedWithMessage(mess));
        } catch (JsonProcessingException e) {
            //log.error(e.getMessage(), e);
        }
        //CommonResult
        DataBuffer buffer = resp.bufferFactory().wrap(returnStr.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}


