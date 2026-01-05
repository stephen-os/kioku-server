# Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Gradle files
COPY settings.gradle .
COPY build.gradle .
COPY gradle gradle
COPY gradlew .

# Fix gradlew permissions
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build with native access enabled
ENV JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"
RUN ./gradlew build -x test

# Runtime stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port (Railway sets PORT env var)
EXPOSE 8080

# Run the application
ENV JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"
ENTRYPOINT ["java", "-jar", "app.jar"]