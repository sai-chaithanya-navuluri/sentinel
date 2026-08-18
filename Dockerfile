# Build stage — compile with Maven, produce the runnable jar
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Run stage — smaller image, no build tooling
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/sentinel-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]