FROM openjdk:11-jre-slim
MAINTAINER @RMaiun
COPY build/libs/cataclysm-2.0.0.jar /opt/cataclysm-2.0.0.jar
ENV USER="" PASS="" HOST="host.docker.internal"
EXPOSE 8080
#ENTRYPOINT ["/usr/bin/java"]
CMD java -jar -Dtoken=$TOKEN -Ddb.user=$USER -Ddb.password=$PASS -Ddb.host=$HOST /opt/cataclysm-2.0.0.jar