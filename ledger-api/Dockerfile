# LemonLedger 后端运行镜像
# 构建（在仓库根目录执行，构建上下文就是 ledger-api）：
#   mvn clean package
#   docker build -t 794862853/lemonledger:2.1 ./ledger-api
FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.title="LemonLedger" \
      org.opencontainers.image.description="成衣厂计件账单/工资系统 后端" \
      org.opencontainers.image.source="https://github.com/ahuixstiao/LemonLedger"

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8"

WORKDIR /app

COPY target/LemonLedger.jar /app/LemonLedger.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/LemonLedger.jar"]
