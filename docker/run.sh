#!/bin/bash

exec java -Xmx64m -XX:+HeapDumpOnOutOfMemoryError \
	-XX:+PrintGC \
    -cp ./doc-store-app.jar:./doc-store-deps.jar \
    ianmorgan.docstore.AppKt