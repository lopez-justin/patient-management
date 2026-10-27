FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY . .

RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/target/*.jar app.jar

USER spring:spring

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
