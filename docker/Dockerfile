FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY apps ./apps
COPY storage ./storage
COPY supplier ./supplier
COPY support ./support

RUN chmod +x ./gradlew

ARG APP_TASK
ARG APP_JAR_GLOB

RUN ./gradlew ${APP_TASK} -x test --no-daemon \
    && cp ${APP_JAR_GLOB} /tmp/app.jar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /tmp/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

