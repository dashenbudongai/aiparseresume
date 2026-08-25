# Multi-stage build: build with Maven, run on a slim JRE
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/resume-cli.jar /app/resume-cli.jar
COPY samples /app/samples
ENV QWEN_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
ENV QWEN_MODEL=qwen-plus
ENTRYPOINT ["java", "-jar", "/app/resume-cli.jar"]
CMD ["--help"]