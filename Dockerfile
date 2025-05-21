FROM maven:3.8.4-openjdk-17 as builder

WORKDIR /app
COPY ./phonestore/pom.xml .
COPY ./phonestore/src ./src

# Build with production profile
RUN mvn clean package -DskipTests -Pprod

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Create directory for file uploads
RUN mkdir -p /app/public/images

# Set environment variable for Railway
ENV RAILWAY_ENVIRONMENT=prod

EXPOSE 8080

CMD ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"] 