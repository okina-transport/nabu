#!/usr/bin/env bash

echo Building docker image

# Back
VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)
IMAGE_NAME=registry.okina.fr/mobiiti/nabu:"${VERSION}"

echo version:${VERSION}
echo targetFile:target/nabu-${VERSION}.jar


docker build -t "${IMAGE_NAME}" --build-arg JAR_FILE=target/nabu-${VERSION}.jar .
docker push "${IMAGE_NAME}"
