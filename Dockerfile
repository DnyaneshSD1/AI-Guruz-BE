FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn -q package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S aiguruz && adduser -S aiguruz -G aiguruz
COPY --from=build /app/target/aiguruz-backend-1.0.0.jar app.jar
RUN mkdir -p logs && chown -R aiguruz:aiguruz /app
USER aiguruz
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]