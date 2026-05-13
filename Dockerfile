# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -x test \
    && JAR=$(find build/libs -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" | head -1) \
    && test -n "$JAR" \
    && cp "$JAR" /app/application.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring --no-create-home spring

COPY --from=builder /app/application.jar /app/app.jar
RUN chown spring:spring /app/app.jar

USER spring:spring
EXPOSE 3000

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
