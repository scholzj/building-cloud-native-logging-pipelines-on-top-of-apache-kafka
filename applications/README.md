# Demo applications

This folder contains the applications used during the demo:
* The Test application whihc just generates some logs
* The Alerting application whihc does monitoring and produces alerts

Both apps are written in Quarkus.

## Build

Run `make all` to build the container image and push it to a registry.
You can use the environment variables `DOCKER_REGISTRY`, `DOCKER_ORG` and `DOCKER_TAG` to configure the registry where the image will be pushed.