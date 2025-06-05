###
# Multi-stage Dockerfile for building and running the BinomeMatcher API
###
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /home/gradle/project

# Copy root build files and subprojects
COPY settings.gradle build.gradle .
COPY core core
COPY security security
COPY api api

WORKDIR /home/gradle/project/api
# Build the Spring Boot fat JAR
RUN gradle bootJar --no-daemon --parallel

FROM eclipse-temurin:17-jre
VOLUME /tmp
EXPOSE 8080

# Copy the built jar from the builder stage
COPY --from=builder /home/gradle/project/api/build/libs/api-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]