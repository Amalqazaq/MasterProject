FROM openjdk:8-jre-alpine

WORKDIR application
COPY lib lib
COPY MasterProject.jar MasterProject.jar
ENTRYPOINT ["/usr/bin/java", "-jar", "MasterProject.jar"]
EXPOSE 8000