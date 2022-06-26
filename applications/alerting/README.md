# Alerting

This project uses Quarkus and Kafka Streams to read logs from the Kafka topic called `logs` and search them for a string `Something ugly happened`.
If the errors happens at least 10 times per minute, an alert will be sent to the Kafka topic named `alerts`.

## Build

Run `make all` to build the container image and push it to a registry.
You can use the environment variables `DOCKER_REGISTRY`, `DOCKER_ORG` and `DOCKER_TAG` to configure the registry where the image will be pushed.