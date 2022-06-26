PROJECT_NAME=building-cloud-native-logging-pipelines-test-app

all: java_build docker_build docker_push
build: java_build
clean: java_clean

include ../Makefile.docker

include ../Makefile.java

.PHONY: build clean
