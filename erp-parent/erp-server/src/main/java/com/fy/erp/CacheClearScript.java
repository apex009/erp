import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication(scanBasePackages = "com.fy.erp")
public class CacheClearScript {
  public static void main(String[] args) {
    ApplicationContext ctx = SpringApplication.run(CacheClearScript.class, args);
    RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) ctx.getBean("redisTemplate");

    System.out.println("Starting to clear auth perm cache...");
    java.util.Set<String> keys = redisTemplate.keys("auth:perm:*");
    if (keys != null && !keys.isEmpty()) {
      System.out.println("Found keys: " + keys.size());
      redisTemplate.delete(keys);
      System.out.println("Deleted keys.");
    } else {
      System.out.println("No auth:perm keys found.");
    }

    System.exit(0);
  }
}
