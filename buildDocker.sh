#!/usr/bin/env bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker build -t graph-store .
docker tag graph-store:latest ianmorgan/graph-store:latest
docker push ianmorgan/graph-store:latest
