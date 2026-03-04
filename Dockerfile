# Pre-built JAR is copied from the host (built via ./mvnw package -DskipTests)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

# Create upload directory
RUN mkdir -p uploads/posts

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
