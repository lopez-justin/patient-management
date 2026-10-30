FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY . .

RUN ./mvnw -q -DskipTests -pl billing-service -am package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/billing-service/target/*.jar app.jar

USER spring:spring

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
