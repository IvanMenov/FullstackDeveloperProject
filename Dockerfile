FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080 5005
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

