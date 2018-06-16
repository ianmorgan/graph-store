#!/bin/bash

exec java -Xmx64m -XX:+HeapDumpOnOutOfMemoryError \
	-XX:+PrintGC \
    -cp /home/app/doc-store-app.jar:/home/app/doc-store-deps.jar \
    ianmorgan.docstore.AppKt