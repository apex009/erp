package com.fy.erp.controller;

import com.fy.erp.constant.RedisKeyPrefix;
import com.fy.erp.exception.BizException;
import com.fy.erp.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/auth/sms")
public class SmsController {

  private final StringRedisTemplate redisTemplate;

  public SmsController(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostMapping("/code")
  public Result<Void> sendCode(@RequestParam String phone, HttpServletRequest request) {
    // 1. 频率限制 (60s)
    String limitKey = RedisKeyPrefix.SMS_LIMIT + phone;
    if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
      throw new BizException(429, "发送过于频繁，请稍后再试");
    }

    // 2. IP 限制 (可选，基于需求)
    String ip = request.getRemoteAddr();
    String ipKey = RedisKeyPrefix.SMS_IP_LIMIT + ip;
    String ipCount = redisTemplate.opsForValue().get(ipKey);
    if (ipCount != null && Integer.parseInt(ipCount) > 10) { // 同IP每天或每小时限制
      throw new BizException(429, "IP请求异常");
    }

    // 3. 生成 6 位验证码
    String code = String.format("%06d", new Random().nextInt(1000000));
    log.info("发送验证码至 {}: {}", phone, code);
    System.out.println("========================================");
    System.out.println("TEST MODE: SMS CODE for " + phone + " is: " + code);
    System.out.println("========================================");

    // 4. 存入 Redis (300s)
    String codeKey = RedisKeyPrefix.SMS_CODE + phone;
    redisTemplate.opsForValue().set(codeKey, code, RedisKeyPrefix.SMS_CODE_TTL.toSeconds(), TimeUnit.SECONDS);

    // 5. 设置频率限制 (60s)
    redisTemplate.opsForValue().set(limitKey, "1", RedisKeyPrefix.SMS_LIMIT_TTL.toSeconds(), TimeUnit.SECONDS);

    // 增加 IP 计数
    redisTemplate.opsForValue().increment(ipKey);
    redisTemplate.expire(ipKey, 1, TimeUnit.HOURS);

    // 模拟发送成功
    return Result.success();
  }
}
