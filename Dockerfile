FROM openjdk:10.0.1-jre
LABEL maintainer="ian.j.morgan@gmail.com"

EXPOSE 7002

RUN mkdir -p /home/app/
RUN mkdir -p /home/app/src/test/resources/starwars
RUN mkdir -p /home/app/src/schema

WORKDIR /home/app
ENTRYPOINT ["./run.sh"]
COPY ./docker/run.sh /home/app/run.sh
RUN chmod +x /home/app/run.sh

# try to reduce times this is updated
COPY ./stash/doc-store-deps.jar /home/app/doc-store-deps.jar


COPY ./src/schema/* /home/app/src/schema/
COPY ./src/test/resources/starwars/* /home/app/src/test/resources/starwars/
COPY ./build/libs/doc-store-app.jar /home/app/doc-store-app.jar


