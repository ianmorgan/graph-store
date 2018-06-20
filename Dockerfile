FROM openjdk:10.0.1-jre
LABEL maintainer="ian.j.morgan@gmail.com"

EXPOSE 7002

RUN mkdir -p /home/app/
RUN mkdir -p /home/app/src/test/resources/starwars
RUN mkdir -p /home/app/src/schema

COPY ./docker/run.sh /home/app/run.sh
RUN chmod +x /home/app/run.sh

# work around to corrupt file run.sh on windows - something to do
# with character encoding
RUN echo "#!/bin/bash" > /home/app/runIt.sh
RUN echo "java -cp /home/app/doc-store-app.jar:/home/app/doc-store-deps.jar ianmorgan.docstore.AppKt" >> /home/app/runIt.sh
RUN chmod +x /home/app/runIt.sh

RUN cat /home/app/runIt.sh
#COPY ./wait-for-it/wait-for-it.sh /home/app/wait-for-it.sh
#RUN chmod +x /home/app/wait-for-it.sh



# try to reduce times this is updated
COPY ./stash/doc-store-deps.jar /home/app/doc-store-deps.jar


COPY ./src/schema/* /home/app/src/schema/
COPY ./src/test/resources/starwars/* /home/app/src/test/resources/starwars/
COPY ./build/libs/doc-store-app.jar /home/app/doc-store-app.jar

RUN ls -l /home/app/

WORKDIR /home/app
ENTRYPOINT ["./runIt.sh"]


