
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build


COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw


RUN ./mvnw dependency:go-offline -q


COPY src ./src
RUN ./mvnw package -DskipTests -q


FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app


COPY --from=builder /build/target/*.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]