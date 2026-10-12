# ----------------------------------------------------------------------------
# Etapa 1: build - compila el jar con Maven, con cache de dependencias
# ----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Primero solo el pom.xml para aprovechar la cache de capas de Docker:
# si no cambian las dependencias, no se vuelven a descargar en cada build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ----------------------------------------------------------------------------
# Etapa 2: runtime - imagen final, liviana, solo con el JRE y el jar
# ----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
