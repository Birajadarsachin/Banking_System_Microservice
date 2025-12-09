# Use official Maven + JDK image to build, then run a slim JRE image
FROM maven:3.9.5-eclipse-temurin-17 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the jar (skip tests for speed; remove -DskipTests=false if you want tests)
RUN mvn -B -DskipTests package

# Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built jar (assumes spring-boot-maven-plugin produced an executable jar)
COPY --from=build /app/target/*.jar app.jar

# Expose Eureka default port
EXPOSE 8761

# Run the app
ENTRYPOINT ["java","-jar","/app/app.jar"]
