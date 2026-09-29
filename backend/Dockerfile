FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
# Ilova root huquqisiz ishlaydi; fayllar /app/data/files ga yoziladi.
RUN groupadd --system app && useradd --system --gid app --home /app app \
    && mkdir -p /app/data/files && chown -R app:app /app
COPY --from=build --chown=app:app /app/target/education-0.1.0.jar app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
