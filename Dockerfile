FROM eclipse-temurin:17-jdk as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests

# REST API Server Image
FROM eclipse-temurin:17-jre as rest-api
VOLUME /tmp
COPY --from=build /workspace/app/target/paypal-java-mcp-server-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]

# JSON-RPC stdio Server Image
FROM eclipse-temurin:17-jre as json-rpc
VOLUME /tmp
COPY --from=build /workspace/app/target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar /app/app.jar

# Create a log directory
RUN mkdir -p /app/logs

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=stdio
ENV LOGGING_FILE_NAME=/app/logs/mcp-server.log

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD java -version || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"] 