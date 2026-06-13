# ─────────────────────────────────────────────
# Stage 1: build do JAR com Maven Wrapper
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia somente os arquivos de dependência primeiro (cache de camadas)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
# Corrige CRLF (Windows) para o script rodar no Alpine Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Baixa dependências sem compilar (aproveita cache Docker)
RUN ./mvnw dependency:go-offline -q

# Copia o código-fonte e compila
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────────────
# Stage 2: imagem de runtime mínima
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=builder /build/target/*.jar app.jar

# Porta exposta pelo Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]