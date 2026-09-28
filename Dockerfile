# Multi-stage build for Render.com (and local Docker).
# Stage 1: Node — install Frontend pin and compile Sass
# Stage 2: Maven — package Spring Boot jar
# Stage 3: JRE — run the service with assets available

FROM node:22-bookworm AS styles
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci
COPY styles ./styles
COPY scripts ./scripts
RUN npm run build:styles

FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml mvnw ./
COPY .mvn ./.mvn
RUN ./mvnw -q -B dependency:go-offline
COPY src ./src
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:25-jre-noble
WORKDIR /app
# Runtime needs compiled CSS, Frontend assets/JS, and baseline policy
COPY --from=styles /build/node_modules/govuk-frontend ./node_modules/govuk-frontend
COPY --from=styles /build/dist ./dist
COPY baseline ./baseline
COPY --from=build /build/target/govuk-frontend-example-java-*.jar ./app.jar
ENV JAVA_OPTS=""
EXPOSE 8080
# Render injects PORT; Spring reads server.port=${PORT:8080}
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
