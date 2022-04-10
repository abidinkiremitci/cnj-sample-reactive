#!/usr/bin/env bash

NS=cnj
APP_NAME=customers
IMAGE_NAME=abidinkiremitci/cnj-basics:latest

#mvn -DskipTests=true clean package spring-boot:build-image \
#  -Dspring-boot.build-image.imageName=${IMAGE_NAME}
#docker push ${IMAGE_NAME}

mkdir -p k8s

kubectl get ns/$NS ||
  kubectl create ns $NS -o yaml >k8s/ns.yaml

kubectl get -n $NS deployment/$APP_NAME ||
  kubectl -n $NS create deployment --image=$IMAGE_NAME $APP_NAME -o yaml >k8s/deployment.yaml
kubectl get -n $NS service/$APP_NAME ||
  kubectl -n $NS expose deployment $APP_NAME --port=8080 -o yaml >k8s/service.yaml
