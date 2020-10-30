FROM openjdk:11-jre-slim
MAINTAINER @RMaiun
RUN apt-get update; apt-get install -y fontconfig libfreetype6
VOLUME /tmp
COPY build/libs/*.jar cataclysm.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /cataclysm.jar ${@}"]