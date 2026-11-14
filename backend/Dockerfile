FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /codigo
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir /app/archivos && chown -R 10001:10001 /app
COPY --from=compilacion /codigo/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/backend.jar"]
