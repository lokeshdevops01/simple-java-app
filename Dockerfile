# eclipse-temurin is the actively maintained OpenJDK build used here.
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /usr/src/app
# Wildcard matches whatever version pom.xml currently declares.
COPY target/*.jar /usr/src/app/simple-java-app.jar
EXPOSE 8080
CMD ["java", "-jar", "/usr/src/app/simple-java-app.jar"]
