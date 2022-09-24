FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

ENV DB_HOST=db
ENV DB_PORT=5432
ENV DB_NAME=shareit
ENV POSTGRES_USER=root
ENV POSTGRES_PASSWORD=root