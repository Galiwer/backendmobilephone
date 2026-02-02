FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /app
COPY ./phonestore/pom.xml .
COPY ./phonestore/src ./src

# Build with production profile
RUN mvn clean package -DskipTests -Pprod


# Runtime stage (FIXED)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Create directory for file uploads
RUN mkdir -p /app/public/images

# Set environment variable for Railway
ENV RAILWAY_ENVIRONMENT=prod

# JVM options for low-memory container
ENV JAVA_OPTS="\
    -XX:MaxRAMPercentage=80.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Xmx400m \
    -Xms200m \
    -XX:+UseSerialGC \
    -XX:+UseStringDeduplication \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    -XX:MetaspaceSize=64m \
    -XX:MaxMetaspaceSize=128m \
    -XX:CompressedClassSpaceSize=32m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/heapdump.hprof \
    -XX:+ExitOnOutOfMemoryError \
    -Xlog:gc*:file=/app/gc.log:time,uptime,level,tags:filecount=2,filesize=5M \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8"

EXPOSE 8080

CMD java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar
