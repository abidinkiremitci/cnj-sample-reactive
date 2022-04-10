#!/usr/bin/env bash

PREFIX=cnj
NAME=docker
IMAGE_NAME=$PREFIX/$NAME

## Docker Native
#mvn -DskipTests clean package
#docker build -t $IMAGE_NAME .

## with packeto.io and buildpacks.io/
pack config default-builder paketobuildpacks/builder:base
pack build $IMAGE_NAME --path . -e BP_JVM_VERSION=17

## Running the image
docker run -p 8088:8088 -e SERVER_PORT=8088 $IMAGE_NAME

