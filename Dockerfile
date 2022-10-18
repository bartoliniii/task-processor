FROM openjdk:17.0-slim

ENV JAVA_OPTS="-Xms50M -Xmx50M -XshowSettings:vm"

EXPOSE 8080
ADD build/libs/*.jar /app.jar

ENTRYPOINT java $JAVA_OPTS -jar app.jar