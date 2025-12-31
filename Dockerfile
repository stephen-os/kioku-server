# Build stage
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build with native access enabled
ENV JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"
RUN ./gradlew build -x test

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Enable native access for runtime
ENV JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]