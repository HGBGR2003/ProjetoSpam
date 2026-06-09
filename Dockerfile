# ─────────────────────────────────────────────
# Stage 1: build do JAR com Maven Wrapper
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia somente os arquivos de dependência primeiro (cache de camadas)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw

# Baixa dependências sem compilar (aproveita cache Docker)
RUN ./mvnw dependency:go-offline -q

# Copia o código-fonte e compila
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────────────
# Stage 2: imagem de runtime mínima
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=builder /build/target/*.jar app.jar

# Porta exposta pelo Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
