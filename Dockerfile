FROM mirriad/mmp-java:latest
LABEL maintainer="marketplace@mirriad.com"

EXPOSE 9912

RUN mkdir -p /home/mirriad/app/datasets
RUN mkdir -p /home/mirriad/app/src/main/resources/mirriaddocs

COPY ./docker/run.sh /home/mirriad/app/run.sh
RUN chmod +x /home/mirriad/app/run.sh

COPY ./src/main/resources/eventfiles/* /home/mirriad/app/datasets/
COPY ./src/main/resources/mirriaddocs/* /home/mirriad/app/src/main/resources/mirriaddocs/
COPY ./build/libs/event-store-service2*.jar /home/mirriad/app/event-store-service2.jar

WORKDIR /home/mirriad/app

ENTRYPOINT ["./run.sh"]