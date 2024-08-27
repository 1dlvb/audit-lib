FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/MessageReceiver-0.0.1-SNAPSHOT.jar /app/MessageReceiver.jar
ENTRYPOINT ["java", "-jar", "/app/MessageReceiver.jar"]