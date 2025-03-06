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
ENTRYPOINT ["java","-jar","/app/app.jar"]

# JSON-RPC stdio Server Image
FROM eclipse-temurin:17-jre as json-rpc
VOLUME /tmp
COPY --from=build /workspace/app/target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"] 