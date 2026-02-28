package com.fy.erp.constant;

import java.time.Duration;

/**
 * Redis Key 常量管理
 */
public interface RedisKeyPrefix {

  // 1. 验证码相关
  String SMS_CODE = "sms:code:"; // 验证码 sms:code:{phone} (TTL 5m)
  String SMS_LIMIT = "sms:limit:"; // 发送频率限制 sms:limit:{phone} (TTL 60s)
  String SMS_IP_LIMIT = "sms:ip:"; // IP 频率限制 sms:ip:{ip} (TTL 60s)

  Duration SMS_CODE_TTL = Duration.ofMinutes(5);
  Duration SMS_LIMIT_TTL = Duration.ofSeconds(60);

  // 2. 权限缓存
  String AUTH_PERM = "auth:perm:"; // 权限列表 auth:perm:{userId} (TTL 2h)
  String AUTH_ROLE_USERS = "auth:role_users:"; // 角色关联用户索引 (TTL 2h)
  Duration AUTH_PERM_TTL = Duration.ofMinutes(120);

  // 3. 字典缓存
  String DICT_WAREHOUSE = "dict:warehouse:all";
  String DICT_SUPPLIER = "dict:supplier:all";
  String DICT_CATEGORY = "dict:category:all";

  Duration DICT_TTL = Duration.ofHours(24);

  // 4. 报表看板缓存
  String REPORT_DASHBOARD = "report:dashboard:"; // report:dashboard:{scope}:{paramsHash}

  Duration REPORT_TTL = Duration.ofMinutes(10);
}
