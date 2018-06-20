
docker build -t graph-store .
docker tag graph-store:latest ianmorgan/graph-store:latest
docker push ianmorgan/graph-store:latest
