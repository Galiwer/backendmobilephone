FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Extremely conservative JVM settings for Railway's 500MB limit
ENV JAVA_OPTS="\
    -XX:MaxRAMPercentage=70.0 \
    -XX:InitialRAMPercentage=40.0 \
    -Xss256k \
    -XX:MaxMetaspaceSize=64m \
    -XX:CompressedClassSpaceSize=16m \
    -XX:ReservedCodeCacheSize=16m \
    -XX:MaxDirectMemorySize=8m \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+UseCompressedOops \
    -XX:+UseCompressedClassPointers \
    -XX:G1HeapRegionSize=1m \
    -XX:MaxGCPauseMillis=200 \
    -XX:+DisableExplicitGC \
    -XX:+OptimizeStringConcat \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -cp app:app/lib/* com.ciro.phonestore.PhoneStoreApplication"] 