# build an intermediate image containing everything required to compile the backend
FROM maven:3-eclipse-temurin-23 AS builder

WORKDIR /app

# install dependencies
COPY pom.xml pom.xml
RUN mvn -B -f pom.xml dependency:resolve

COPY . .

# build an image containing everything required to compile study plugins
FROM builder AS plugins-compiler

LABEL org.opencontainers.image.authors="info@rodano.ch"
LABEL org.opencontainers.image.description="Rodano image containing the bits required to compile study plugins"

RUN mvn -B install -DskipTests

# build an intermediate image containing a fat JAR for the backend
FROM builder AS packager

RUN mvn -B package -DskipTests

# build a light image containing only the backend
FROM eclipse-temurin:23

LABEL org.opencontainers.image.authors="info@rodano.ch"
LABEL org.opencontainers.image.description="Rodano backend"

WORKDIR /app

COPY --from=packager /app/target/rodano-backend-exec.jar rodano.jar

# app plugins may be provided inside the /app/plugins folder
ENTRYPOINT ["java", "-cp", "/app/rodano.jar", "-Dloader.path=/app/plugins", "org.springframework.boot.loader.launch.PropertiesLauncher"]
