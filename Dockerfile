FROM openjdk:8-jdk-alpine

#Le instalamos curl. Que viene bien para ver conectividad.
RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*

VOLUME /tmp
ARG JAR_FILE
COPY build/libs/bitcoindAdapter-0.0.1-SNAPSHOT.jar app.jar
#COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

#FROM openjdk:8-jdk-alpine
#VOLUME /tmp
#ARG JAR_FILE
#COPY ${JAR_FILE} app.jar
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]