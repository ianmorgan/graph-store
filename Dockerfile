FROM openjdk:10.0.1-jre
LABEL maintainer="ian.j.morgan@gmail.com"

EXPOSE 7002

RUN mkdir -p /home/app/
RUN mkdir -p /home/app/src/test/resources/starwars
RUN mkdir -p /home/app/src/schema

COPY ./docker/run.sh /home/app/run.sh
RUN chmod +x /home/app/run.sh

COPY ./src/schema/* /home/app/src/schema/
COPY ./src/test/resources/starwars/* /home/app/src/test/resources/starwars/
COPY ./build/libs/doc-store*.jar /home/app/doc-store.jar

WORKDIR /home/app

ENTRYPOINT ["./run.sh"]