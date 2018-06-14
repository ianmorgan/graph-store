#!/bin/bash


exec java -Xmx64m -XX:+HeapDumpOnOutOfMemoryError \
	-XX:+PrintGC \
    -cp /home/app/doc-store.jar \
    ianmorgan.docstore.AppKt