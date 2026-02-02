# =========================
# Stage 1: Build
# =========================
FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# Copiar archivos de configuración para cachear dependencias
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

# Copiar el código fuente
COPY src ./src

# Construir el JAR ejecutable
RUN gradle bootJar --no-daemon

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto estándar para contenedores / Render
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java","-jar","app.jar"]
