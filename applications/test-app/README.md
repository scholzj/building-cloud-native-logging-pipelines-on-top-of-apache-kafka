# Test App

This app is used as a simple demo application during the demos

It had 3 endpoints:
* `/hello`
* `/uglyError`
* `/manyUglyErrors`

It also triggers the `/uglyError` endpoint every 10 seconds.

## Build

Run `make all` to build the container image and push it to a registry.
You can use the environment variables `DOCKER_REGISTRY`, `DOCKER_ORG` and `DOCKER_TAG` to configure the registry where the image will be pushed.