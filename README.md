# Building Cloud-native Logging Pipelines on top of Apache Kafka

This repository container the demo for my talk at the [Kubernetes Community Days Berlin 2022](https://community.cncf.io/events/details/cncf-kcd-berlin-presents-kubernetes-community-days-berlin-2022-1/) conference about Apache Kafka as a Monitoring Data Pipeline.

## Slides

The slides can be found in [Google Docs](https://docs.google.com/presentation/d/1sHgFBJyrM0Q3WMDEqNyQy2xSXgONgapFPazUgo2RWmY/edit?usp=sharing).

## Demo

### Preparation

1) Install the Strimzi operator.
   The demo is currently using Strimzi 0.29.0, but it should work also with newer versions.
   If needed, follow the documentation at [https://strimzi.io](https://strimzi.io).
   The namespace used during the demo us `myproject` - you can choose your own, but you will need to update the YAML files to match your namespace.

#### Create the required Secrets with credentials

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

#### Deploy Kafka and Kafka Connect

5) Deploy the Kafka cluster:
   ```
   kubectl apply -f 01-kafka.yaml
   ```

6) Once Kafka cluster is ready, deploy the Kafka Connect cluster which will also download the Camel Kafka Connectors for Twitter
   ```
   kubectl apply -f 02-connect.yaml
   ```

#### Deploy ElasticSearch and Kibana

7) Next we deploy ElasticSearch to use for looking through the logs.
   The YAMLs for both are in [`03-elasticsearch.yaml`](./03-elasticsearch.yaml) and [`04-kibana.yaml`](./04-kibana.yaml).
   ```
   kubectl apply -f 03-elasticsearch.yaml
   kubectl apply -f 04-kibana.yaml
   ```

#### Deploy Fluentbit

8) First we have to create a topic where the logs will be sent.
   The definition for the Strimzi operator is in [`05-topic-logs.yaml`](./05-topic-logs.yaml).
   Create it with `kubectl apply`.
   ```
   kubectl apply -f 05-topic-logs.yaml
   ```

9) Next we have to deploy Fluent-bit and configure it to send the logs to Kafka.
   We have to create all the different RBAC resource and a config map with configuration.
   The FLuent-bit it self runs as DeamonSet to collect the logs from all nodes.
   The whole Fluent Bit deployment is in [`06-fluentbit.yaml`](./06-fluentbit.yaml)
   ```
   kubectl apply -f 06-fluentbit.yaml
   ```

#### Push data to ElasticSearch

10) Now with everything running, we can use the Apache Camel Elastic Search connector to push the logs to ElasticSearch.
    We can deploy it using the [`07-elasticsearch-connector.yaml`](./07-elasticsearch-connector.yaml).
    ```
    kubectl apply -f 07-elasticsearch-connector.yaml
    ```

11) Create the indexes in Kibana to make it easy to search for logs later

#### Push the data to Amazon AWS S3

12) Deploy the Apache Camel S3 connector to push the data to Amazon AWS S3.
    First, make sure the bucket is created.
    Then deploy the S3 connector from [`08-s3-connector.yaml`](./08-s3-connector.yaml):
    ```
    kubectl apply -f 08-s3-connector.yaml
    ```
    And go back to Amazon AWS S3 to check the data in the S3 bucket.

### Demo 1

13) Show the deployment files.
    Focus mainly on:
    * Kafka & Kafka Connect
    * Fluentbit
    * ElasticSearch Connector

14) Check the messages produced by Fluentbit by consuming from the `logs` Kafka topic.
    You can use the helper script for it:
    ```
    ./10-check-logs.sh
    ```

15) Show that the data are really in Kibana

### Demo 2

#### S3 archiving

16) Show the S3 Connector.
    And show that the data are really flowing into the S3 bucket.

#### Detect anomalies and send them to Slack

17) Create a new topic for alerts.
    Any message sent to this topic will be later forwarded to our Slack channel.
    So the apps processing the logs and triggering alerts can just send the alerts here.
    The topic is defined in [`20-topic-alerts.yaml`](./20-topic-alerts.yaml).
    We just need to apply it:
    ```
    kubectl apply -f 20-topic-alerts.yaml
    ```

18) Next, deploy the Apache Camel Slack connector which will read the alerts form the topic and forward it to Slack.
    One of the advantages of Kafka is that it integrates with many different systems which you can use.
    And if needed you can easily write your own.
    You can check the connector definition in [`21-slack-connector.yaml`](./21-slack-connector.yaml) and create it:
    ```
    kubectl apply -f 21-slack-connector.yaml
    ```

19) Deploy the test application to produce the error logs.
    ```
    kubectl apply -f 22-test-app.yaml
    ```
    You can check the source codes in  [`./applications/test-app`](./applications/test-app).

20) Check the alerting application based on Kafka Streams API.
    The alerting application can be deployed from the [`23-alerting.yaml`](./232-alerting.yaml) file:
    ```
    kubectl apply -f 23-alerting.yaml
    ```
    You can check the source codes in  [`./applications/alerting`](./applications/alerting).

21) Trigger some alerts in the Test application and check that the trigger messages to be sent to the alert topic:
    You can use the helper script for it:
    ```
    ./24-trigger-alerts.sh
    ./25-check-alerts.sh
    ```

22) Check the messages on Slack in the channel you configured your Slack connector to push the messages to.

### _Notes to executing the demo_

* _The source codes of the Test and Alerting apps used during the demo can be found in [`./applications`](./applications)_
* _You need to change the Ingress addresses or use another method of exposing the apps to match your environment_
* _You need to change the things such as the S3 bucket, Slack room, ElasticSearch URL etc. to match your setup_
