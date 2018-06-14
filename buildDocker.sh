
docker build -t doc-store .
docker tag doc-store:latest ianmorgan/doc-store:latest
docker push ianmorgan/doc-store:latest
