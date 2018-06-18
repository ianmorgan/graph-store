# Docker Support 

## Running under Docker 

```bash
docker run -d -p 7002:7002 ianmorgan/doc-store
```

This starts the service with the in-memory event-store and the demo StarWars schema and data 
 
Can test running instance on [AWS](http://34.246.171.255:7002/docs/Human/1000).

For production style deploys see the example [Docker Compose](https://github.com/ianmorgan/docker-stacks) scripts (_still 
very much work in progress_)

## Building and Publishing 

### building doc-store-deps.jar

For efficiency, the docker build is in 2 steps. If the dependencies have changed, 
or this is the first time a docker build has been run, build the uber jar with all dependencies with 

```bash
./gradlew clean compileKotlin depsJar stashDepsJar
```

Minimising rebuilds on this jar greatly reduces the number of new layers in the docker image, which speeds up all 
the docker build & publish & pull steps

### build doc-store-app.jar

```bash
./gradlew clean appJar 
```

### build image and publish 

```bash
docker build -t doc-store .
docker tag doc-store:latest ianmorgan/doc-store:latest
docker push ianmorgan/doc-store:latest
```




