# 1. 基础镜像使用带JDK17的OpenJDK
FROM eclipse-temurin:17-jdk

# 2. 设置工作目录
WORKDIR /app

# 3. 拷贝后端JAR
COPY target/LemonLedger.jar /app/LemonLedger.jar

# 4. 运行端口
EXPOSE 8080

# 5. 启动命令
ENTRYPOINT ["java", "-jar", "LemonLedger.jar"]
