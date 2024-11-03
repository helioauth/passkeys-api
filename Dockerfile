#
# Build stage
#
FROM eclipse-temurin:22-jdk-jammy AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 chmod +x ./mvnw \
    && ./mvnw -f $HOME/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:22-jre-jammy
ARG JAR_FILE=/usr/app/target/*.jar
COPY --from=build $JAR_FILE /app/runner.jar
EXPOSE 8080
ENTRYPOINT java -jar /app/runner.jar