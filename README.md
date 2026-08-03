# sensorTracking

This repository contains a simple Quarkus-based sensor tracking demo with two modules:

- remoteSensor: a small producer app that generates sensor data and publishes it to Kafka.
- sensorDisplay: a consumer app that subscribes to the same Kafka topic and renders the incoming telemetry in a browser-based UI.

The demo is intended to show how a remote sensor can send data through Kafka to a display application using Quarkus and Kafka SSL configuration.

## Prerequisites

Before you start, make sure you have:

- Java 17+ and Maven
- A running Kafka broker with TLS enabled
- Access to the broker CA certificate (or cluster CA secret)

## Getting started

1. Clone the repository and build the parent project:

   ```bash
   ./mvnw package
   ```

2. Create a Java truststore for the Kafka broker certificate.
   If you are using OpenShift or AMQ Streams, the CA certificate is often exposed through a secret. The example below uses the OpenShift CLI:

   ```bash
   oc get secret sensorkafka-cluster-ca-cert -o jsonpath='{.data.ca\.crt}' | base64 -d > /tmp/kafka-ca.crt
   keytool -import -trustcacerts -file /tmp/kafka-ca.crt -alias kafka-ca -keystore /home/daanders/Work/Demos/sensorTracking/mykeystore.jks -storepass sensorcapass -noprompt
   ```

   If you use kubectl instead of oc, the same idea applies:

   ```bash
   kubectl get secret sensorkafka-cluster-ca-cert -o jsonpath='{.data.ca\.crt}' | base64 -d > /tmp/kafka-ca.crt
   keytool -import -trustcacerts -file /tmp/kafka-ca.crt -alias kafka-ca -keystore /home/daanders/Work/Demos/sensorTracking/mykeystore.jks -storepass sensorcapass -noprompt
   ```

   Replace the secret name if your environment uses a different Kafka CA secret.

3. Point the applications at the truststore by exporting the expected environment variables:

   ```bash
   export KAFKA_BOOTSTRAP_SERVERS=sensorkafka-kafka-tls-bootstrap-sensortracking.apps.ussredhat.dbarh.demo:443
   export KAFKA_SSL_TRUSTSTORE_LOCATION=/home/daanders/Work/Demos/sensorTracking/mykeystore.jks
   export KAFKA_SSL_TRUSTSTORE_PASSWORD=sensorcapass
   ```

4. Start the producer and consumer modules in separate terminals:

   ```bash
   ./mvnw -pl remoteSensor quarkus:dev
   ```

   ```bash
   ./mvnw -pl sensorDisplay quarkus:dev
   ```

5. Open the display UI in your browser:

   - sensorDisplay: http://localhost:8080
   - remoteSensor: http://localhost:8081

## Module overview

- remoteSensor publishes JSON sensor payloads to the Kafka topic sensor-data.
- sensorDisplay consumes those events and displays them in the browser.

## Notes

The Quarkus configuration already expects a TLS-enabled Kafka connection and a Java keystore truststore at the path configured in the application properties. If your Kafka broker uses a different hostname, secret name, or truststore password, update the environment variables or the application properties accordingly.
