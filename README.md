# Building Cloud-native Logging Pipelines on top of Apache Kafka

This repository container the demo for my talk at the [Kubernetes Community Days Berlin 2022](https://community.cncf.io/events/details/cncf-kcd-berlin-presents-kubernetes-community-days-berlin-2022-1/) conference about Apache Kafka as a Monitoring Data Pipeline.

## Slides

The slides can be found in [Google Docs](https://docs.google.com/presentation/d/1sHgFBJyrM0Q3WMDEqNyQy2xSXgONgapFPazUgo2RWmY/edit?usp=sharing).

## Demo Prerequisites

The prerequisites were deployed before the actual demo to save time.

### Namespace

This demo expects to be run in the namespace `myproject`.
Before running the demo, create it and set it as the default namespace.

### Secrets

#### Docker secret

Create Docker credentials secret names `kafkaconnectbuild-pull-secret`.
It should be secret of type `kubernetes.io/dockerconfigjson` and contain credentials to some Container registry account which will be used for the Kafka Connect build.
This secret is referenced in [`02-kafka-connect.yaml`](./02-kafka-connect.yaml).
You might need to also edit the container image used in [`02-kafka-connect.yaml`](./02-kafka-connect.yaml) to match your own container registry.

#### AWS Credentials

Create a secret with AWS credentials for uploading logs to S3.
The secret should contain a file named `aws-credentials.properties`.
This file should contain following properties:

```properties
    aws.access-key=XXXX
    aws.secret-key=YYYY
    aws.region=REGION
```

You should also create a bucket to which these credentials will have the write access and update the file [`09-s3-connector.yaml`](./09-s3-connector.yaml) with this bucket.

#### Slack Webhook URL

Create a Slack Webhook URL for publishing messages to your Slack.
Create a secret with the Slack URL in a properties file which should look something like this:

```properties
webhookUrl=https://hooks.slack.com/services/XXXXX/YYYYY/ZZZZZ
```

You might need to edit the [`11-slack-connector.yaml`](./11-slack-connector.yaml) and adjust the channel name to match 

### Strimzi Kafka Operator

To deploy and run Kafka, we will use Strimzi operator.
Before we deploy Kafka, we have to install the operator:

```
kubectl apply -f 00-strimzi/
```

### Kafka cluster

Next, we have to deploy the Kafka cluster.
The configuration of the deployment is in [`01-kafka.yaml`](./01-kafka.yaml) which has to be applied.

```
kubectl apply -f 01-kafka.yaml
```

### Kafka Connect

One of the easiest way to get messages out of Kafka is to use Kafka Connect and different Connectors.
So next we will deploy Kafka Connect with additional connectors - ElasticSearch Connector, AWS S3 connector and Slack connector.
We will use these later to get the data from Kafka.
The definition is in [`02-kafka-connect.yaml`](./02-kafka-connect.yaml).

```
kubectl apply -f 02-kafka-connect.yaml
```

### Deploy ElasticSearch and Kibana

Next we deploy ElasticSearch to use for looking through the logs.
The YAMLs for both are in [`03-elasticsearch.yaml`](./03-elasticsearch.yaml) and [`04-kibana.yaml`](./04-kibana.yaml).

```
kubectl apply -f 03-elasticsearch.yaml
kubectl apply -f 04-kibana.yaml
```

## Demo 1

### Logs topic

First we have to create a topic where the logs will be sent.
The definition for the Strimzi operator is in [`05-fluent-bit-logs-topic.yaml`](./05-fluent-bit-logs-topic.yaml).
Create it with `kubectl apply`.

```
kubectl apply -f 05-fluent-bit-logs-topic.yaml
```

### Fluent Bit

Next we have to deploy Fluent-bit and configure it to send the logs to Kafka.
We have to create all the different RBAC resource and a config map with configuration.
The FLuent-bit it self runs as DeamonSet to collect the logs from all nodes.
The whole Fluent Bit deployment is in [`04-fluentbit.yaml`](./04-fluentbit.yaml)

```
kubectl apply -f 04-fluentbit.yaml
```

### Check log messages in Kafka

#### Console User

Create a user which we can use from localhost to check the messages and extract the certificates

```
kubectl apply -f 07-console-user.yaml
kubectl get secret console -o jsonpath="{.data.user\.crt}" | base64 --decode > user.crt
kubectl get secret console -o jsonpath="{.data.user\.key}" | base64 --decode > user.key
kubectl get secret my-cluster-cluster-ca-cert -o jsonpath="{.data.ca\.crt}" | base64 --decode > ca.crt
```

#### Check messages with Kafkacat

Get the address of the Kafka broker from the status in `kubectl get kafka -o yaml`
And use it with Kafkacat to see the messages from Fluent Bit.

```
kafkacat -C -b 192.168.1.222:9094 -X security.protocol=ssl -X ssl.ca.location=ca.crt -X ssl.certificate.location=user.crt -X ssl.key.location=user.key -t fluent-bit-logs -f '%s\n\n' -o end
```

We should see the messages in JSON format being received.

### Push data to ElasticSearch

Explain Kafka Connect and check the deployment

Now with everything running, we can use the Camel Elastic Search connector to push the logs to ElasticSearch.
We can deploy it using the [`08-elasticsearch-connector.yaml`](./08-elasticsearch-connector.yaml).

```
kubectl apply -f 08-elasticsearch-connector.yaml
```

#### Check the data in Kibana

After it is deployed and running, we can go to Kibana, create the index and check the data.

## Demo 2

### Archive data to Amazon AWS S3

Deploy the Apache Camel S3 connector to push the data to Amazon AWS S3.
First show the empty bucket.
Then deploy the S3 connector from [`09-s3-connector.yaml`](./09-s3-connector.yaml):

```
kubectl apply -f 09-s3-connector.yaml
```

And go back to Amazon AWS S3 to check the data in the S3 bucket.

### Slack alerting

#### Alerts topic

First we need to create the topic for alerts.
Any message sent to this topic will be later forwarded to our Slack channel.
So the apps processing the logs and triggering alerts can just send the alerts here.
The topic is defined in [`10-logging-alerts-topic.yaml`](./10-logging-alerts-topic.yaml).
We just need to apply it:

```
kubectl apply -f 10-logging-alerts-topic.yaml
```

#### Slack Connector

Next we deploy the Apache Camel Slack connector which will read the alerts form the topic and forward it to Slack.
One of the advantages of Kafka is that it integrates with many different systems which you can use.
And if needed you can easily write your own.
You can check the connector definition in [`11-slack-connector.yaml`](./11-slack-connector.yaml) and create it:

```
kubectl apply -f 11-slack-connector.yaml
```

#### Alerting / Stream processing

Check the alerting application based on Kafka Streams API.
It is very simple just for this demo.
But it obviously can be improved. 
For example extract the CNs or IPs and search for patterns etc.

The alerting application can be deployed from the [`12-alerting.yaml`](./12-alerting.yaml) file:

```
kubectl apply -f 12-alerting.yaml
```

#### Trigger some alerts

We can now test the alerting.
Lets use our Kafkacat consumer and try to access some forbidden topic:

```
kafkacat -C -b 192.168.1.222:9094 -X security.protocol=ssl -X ssl.ca.location=ca.crt -X ssl.certificate.location=user.crt -X ssl.key.location=user.key -t logging-alerts -f '%s\n\n' -o end
```

Now check Slack client and the alerts should arrive soon!
