FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY . .

RUN ./mvnw -q -DskipTests -pl analytics-service -am package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/analytics-service/target/*.jar app.jar

USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
