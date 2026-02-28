package com.fy.erp.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class OrderNoUtil {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  public static String generate(String prefix) {
    String time = LocalDateTime.now().format(FORMATTER);
    int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
    return prefix + time + rand;
  }
}
