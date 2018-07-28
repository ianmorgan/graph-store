FROM openjdk:10.0.1-jre
LABEL maintainer="ian.j.morgan@gmail.com"

EXPOSE 7002

RUN mkdir -p /home/app/
RUN mkdir -p /home/app/src/test/resources/starwars
RUN mkdir -p /home/app/src/test/resources/starwars_ex

RUN mkdir -p /home/app/src/schema
WORKDIR /home/app

# work around to corrupt file run.sh when buidling docker containers on windows. Something to do
# with character encoding
RUN echo "#!/bin/bash" > /home/app/runIt.sh
RUN echo "java -Xmx64m -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGC -cp /home/app/doc-store-app.jar:/home/app/doc-store-deps.jar ianmorgan.graphstore.AppKt" >> /home/app/runIt.sh
RUN chmod +x /home/app/runIt.sh

# hopefully this will stay the same between docker builds
COPY ./stash/doc-store-deps.jar /home/app/doc-store-deps.jar

# the actual application code, which will change each time.
COPY ./src/schema/* /home/app/src/schema/
COPY ./src/test/resources/starwars/* /home/app/src/test/resources/starwars/
COPY ./src/test/resources/starwars_ex/* /home/app/src/test/resources/starwars_ex/
COPY ./build/libs/doc-store-app.jar /home/app/doc-store-app.jar


ENTRYPOINT ["./runIt.sh"]


