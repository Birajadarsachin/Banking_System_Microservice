# Build stage
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy entire project
COPY . .

# Use Maven Wrapper + custom settings
RUN chmod +x mvnw
RUN ./mvnw -B -s .mvn/settings.xml -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
