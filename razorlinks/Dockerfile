# Use a Java 17 JDK image to build the Spring Boot app
FROM eclipse-temurin:17-jdk AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper files
COPY gradlew ./
COPY gradle/ gradle/

# Ensure the Gradle wrapper is executable
RUN chmod +x gradlew

# Copy the build configuration files
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy the source code and build the application
COPY src ./src
RUN ./gradlew build --no-daemon -x test

# Use a Java 17 runtime image to run the application
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Specify the command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]