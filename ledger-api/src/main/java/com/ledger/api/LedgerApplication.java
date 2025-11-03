package com.ledger.api;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@Slf4j
@EnableTransactionManagement //启用事务管理
@MapperScan("com.ledger.db.mapper")
@SpringBootApplication(scanBasePackages = {"com.ledger.api", "com.ledger.db", "com.ledger.common"}) // 扫描包的组件比如@Component、@Configuration
public class LedgerApplication {

    /* 启动类 */
    public static void main(String[] args) {
        disableWarning();
        SpringApplication.run(LedgerApplication.class, args);
        log.info("====================== LemonLedger Application Started ======================");
    }

    // 关闭警告
    public static void disableWarning() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe u = (Unsafe) theUnsafe.get(null);

            Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
        } catch (Exception e) {
            // ignore
        }
    }

}
