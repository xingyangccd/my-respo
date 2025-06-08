FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# 下载依赖
RUN mvn dependency:go-offline

# 复制源代码
COPY src ./src

# 构建应用
RUN mvn package -DskipTests

# 运行阶段
FROM openjdk:17-slim
WORKDIR /app

# 复制构建好的jar包
COPY --from=build /app/target/*.jar app.jar

# 创建上传目录
RUN mkdir -p /app/uploads
VOLUME /app/uploads

# 暴露端口
EXPOSE 8080

# 定义环境变量
ENV SPRING_PROFILES_ACTIVE=prod

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 