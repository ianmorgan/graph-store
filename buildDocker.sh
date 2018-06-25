#!/usr/bin/env bash

./gradlew jar appJar depsJar stashDepsJar

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build -t graph-store .
docker tag graph-store:latest ianmorgan/graph-store:travis
docker push ianmorgan/graph-store:travis
