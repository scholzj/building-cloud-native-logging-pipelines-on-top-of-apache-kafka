#!/usr/bin/env bash

kubectl run helper-pod --image=quay.io/strimzi/kafka:0.29.0-kafka-3.2.0 --restart=Never -- sleep 3600
kubectl wait pod/helper-pod --for=condition=Ready --timeout=300s
kubectl exec helper-pod -- curl test-app:8080/manyUglyErrors
