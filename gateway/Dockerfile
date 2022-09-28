FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

ENV DB_HOST=server
ENV DB_PORT=9090