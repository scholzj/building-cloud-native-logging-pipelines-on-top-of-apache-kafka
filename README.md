# Building Cloud-native Logging Pipelines on top of Apache Kafka

This repository container the demo for my talk at the [Kubernetes Community Days Berlin 2022](https://community.cncf.io/events/details/cncf-kcd-berlin-presents-kubernetes-community-days-berlin-2022-1/) conference about Apache Kafka as a Monitoring Data Pipeline.

## Slides

The slides can be found in [Google Docs](https://docs.google.com/presentation/d/1sHgFBJyrM0Q3WMDEqNyQy2xSXgONgapFPazUgo2RWmY/edit?usp=sharing).

## Demo

### Prerequisites

1) Install the Strimzi operator.
   The demo is currently using Strimzi 0.29.0, but it should work also with newer versions.
   If needed, follow the documentation at [https://strimzi.io](https://strimzi.io).
   The namespace used during the demo us `myproject`, but you should be of course able to choose your own.

2) Create a Kubernetes Secret with credentials for your container registry.
   It should be secret of type `kubernetes.io/dockerconfigjson` and contain credentials to some Container registry account which will be used for the Kafka Connect build.
   This secret is referenced in [`02-kafka-connect.yaml`](./02-kafka-connect.yaml).
   You might need to also edit the container image used in [`02-kafka-connect.yaml`](./02-kafka-connect.yaml) to match your own container registry.
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
       name: docker-credentials
   type: kubernetes.io/dockerconfigjson
   data:
       .dockerconfigjson: Cg==
   ```
   If you are going to use the OpenShift built-in registry, please eliminate this part and refer to the comment section in `02-connect.yaml`.

3) Create a secret with AWS credentials for uploading logs to S3.
   This Secret should contain following fields:
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
       name: aws-credentials
   type: Opaque
   data:
       aws.access-key: Cg==
       aws.secret-key: Cg==
       aws.region: us-east-1
   ```
   You should also create a bucket to which these credentials will have the write access and update the file [`09-s3-connector.yaml`](./09-s3-connector.yaml) with this bucket.

4) Create a Secret with Slack Webhook URL for publishing messages to your Slack:
   ```yaml
   apiVersion: v1
   kind: Secret
   metadata:
       name: slack-credentials
   type: Opaque
   data:
       webhookUrl: Cg==
   ```
   You might need to edit the [`11-slack-connector.yaml`](./11-slack-connector.yaml) and adjust the channel name to match.

5) Deploy the Kafka cluster:
   ```
   kubectl apply -f 01-kafka.yaml
   ```

6) Once Kafka cluster is ready, deploy the Kafka Connect cluster which will also download the Camel Kafka Connectors for Twitter
   ```
   kubectl apply -f 02-connect.yaml
   ```

### Deploy ElasticSearch and Kibana

7) Next we deploy ElasticSearch to use for looking through the logs.
   The YAMLs for both are in [`03-elasticsearch.yaml`](./03-elasticsearch.yaml) and [`04-kibana.yaml`](./04-kibana.yaml).
   ```
   kubectl apply -f 03-elasticsearch.yaml
   kubectl apply -f 04-kibana.yaml
   ```

### Deploy Fluentbit

8) First we have to create a topic where the logs will be sent.
   The definition for the Strimzi operator is in [`05-topic-logs.yaml`](./05-topic-logs.yaml).
   Create it with `kubectl apply`.
   ```
   kubectl apply -f 05-topic-logs.yaml
   ```

9) Next we have to deploy Fluent-bit and configure it to send the logs to Kafka.
   We have to create all the different RBAC resource and a config map with configuration.
   The FLuent-bit it self runs as DeamonSet to collect the logs from all nodes.
   The whole Fluent Bit deployment is in [`04-fluentbit.yaml`](./04-fluentbit.yaml)
   ```
   kubectl apply -f 04-fluentbit.yaml
   ```

10) Check the messages produced by Fluentbit:
    TODO: Use consumer running inside the cluster

### Push data to ElasticSearch

11) Now with everything running, we can use the Apache Camel Elastic Search connector to push the logs to ElasticSearch.
    We can deploy it using the [`08-elasticsearch-connector.yaml`](./08-elasticsearch-connector.yaml).
    ```
    kubectl apply -f 08-elasticsearch-connector.yaml
    ```

12) Check the data in Kibana

### Push the data to Amazon AWS S3

13) Deploy the Apache Camel S3 connector to push the data to Amazon AWS S3.
    First, make sure the bucket is created
    Then deploy the S3 connector from [`09-s3-connector.yaml`](./09-s3-connector.yaml):
    ```
    kubectl apply -f 09-s3-connector.yaml
    ```
    And go back to Amazon AWS S3 to check the data in the S3 bucket.

### Detect anomalies and send them to Slack

14) Create a new topic for alerts.
    Any message sent to this topic will be later forwarded to our Slack channel.
    So the apps processing the logs and triggering alerts can just send the alerts here.
    The topic is defined in [`10-topic-alerts.yaml`](./10-topic-alerts.yaml).
    We just need to apply it:
    ```
    kubectl apply -f 10-topic-alerts.yaml
    ```

15) Next, deploy the Apache Camel Slack connector which will read the alerts form the topic and forward it to Slack.
    One of the advantages of Kafka is that it integrates with many different systems which you can use.
    And if needed you can easily write your own.
    You can check the connector definition in [`11-slack-connector.yaml`](./11-slack-connector.yaml) and create it:
    ```
    kubectl apply -f 11-slack-connector.yaml
    ```

16) Check the alerting application based on Kafka Streams API.
    The alerting application can be deployed from the [`12-alerting.yaml`](./12-alerting.yaml) file:
    ```
    kubectl apply -f 12-alerting.yaml
    ```

17) Trigger some alerts and check that the trigger messages to be sent to the alert topic:
    TODO: Consume the mesages form inside the cluster

18) Check the messages on Slack.
