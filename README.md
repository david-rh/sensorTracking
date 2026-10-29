# sensorTracking

A Quarkus-based GPS sensor tracking demo. A remote sensor publishes simulated GPS telemetry to Kafka over TLS, and a display application consumes the data and renders moving markers on a dark-themed map.

## Project structure

```
sensorTracking/
├── remoteSensor/         # Quarkus producer — generates and publishes GPS data to Kafka
├── sensorDisplay/        # Quarkus consumer — renders sensor positions on a Leaflet map
│   └── deploy/           # Kubernetes/OpenShift manifests (Deployment, Service, Route, ConfigMap, Secret)
├── kafka/                # AMQ Streams / Strimzi Kafka manifests with Kustomize
├── ansible/              # Automation playbooks (cert management, build, deploy)
└── pom.xml               # Parent POM (multi-module)
```

## Prerequisites

- Java 25 and Maven (or use the included `mvnw` wrapper)
- Podman (used for container builds)
- A Kafka broker with TLS enabled (see [Deploying Kafka](#deploying-kafka))
- `oc` CLI for OpenShift deployment
- Ansible for running automation playbooks

## Local development

1. Build both modules:

   ```bash
   ./mvnw package
   ```

2. Set up the Kafka TLS truststore. You can do this manually or use the Ansible playbook (see [Certificate management](#certificate-management)):

   ```bash
   export KAFKA_BOOTSTRAP_SERVERS=<your-kafka-bootstrap>:443
   export KAFKA_SSL_TRUSTSTORE_LOCATION=$(pwd)/mykeystore.jks
   export KAFKA_SSL_TRUSTSTORE_PASSWORD=sensorcapass
   ```

3. Start the producer and consumer in separate terminals:

   ```bash
   ./mvnw -pl remoteSensor quarkus:dev
   ```

   ```bash
   ./mvnw -pl sensorDisplay quarkus:dev
   ```

4. Open the display UI:
   - sensorDisplay: http://localhost:8080
   - remoteSensor health: http://localhost:8081

## Deploying Kafka

The `kafka/` directory contains AMQ Streams (Strimzi) manifests managed with Kustomize. This creates the `sensortracking` namespace, installs the AMQ Streams operator, deploys a Kafka cluster, and creates the `sensor-data` topic.

```bash
oc apply -k kafka/
```

## Certificate management

The `ansible/fetch-certs.yaml` playbook fetches TLS certificates from Kafka broker endpoints and imports them into a Java keystore.

Edit the `cert_urls` variable in the playbook to list your broker endpoints, then run:

```bash
ansible-playbook ansible/fetch-certs.yaml
```

Variables:
| Variable | Default | Description |
|---|---|---|
| `cert_urls` | — | List of `{host, port}` endpoints to fetch certs from |
| `keystore_path` | `../mykeystore.jks` | Path to the output JKS file |
| `keystore_password` | `sensorcapass` | Keystore password |

The playbook is idempotent — re-running it skips certificates that are already in the keystore.

## Building and pushing the sensorDisplay image

The `ansible/build-sensor-display.yaml` playbook builds the Quarkus application, creates a container image with Podman, and pushes it to Quay.io.

```bash
ansible-playbook ansible/build-sensor-display.yaml
```

The image is pushed to `quay.io/daanders-rh/sensor-display:latest`. Override the registry variables in the playbook to use a different registry or tag.

## Deploying sensorDisplay to OpenShift

The `ansible/deploy-sensor-display.yaml` playbook creates the Kafka truststore secret, applies the Kubernetes manifests from `sensorDisplay/deploy/`, restarts the deployment, and waits for rollout.

```bash
ansible-playbook ansible/deploy-sensor-display.yaml
```

The deployment manifests include:
- **ConfigMap** — `KAFKA_BOOTSTRAP_SERVERS` and truststore location
- **Secret** — truststore password
- **Deployment** — pulls from Quay with `imagePullPolicy: Always`, mounts the truststore volume, and includes health probes (`/q/health/ready`, `/q/health/live`)
- **Service** — exposes port 8080
- **Route** — TLS edge termination

## remoteSensor: bootc Image Mode (edge deployment)

The remoteSensor can be built as a RHEL Image Mode (bootc) image for deployment to bare-metal edge devices. This produces a bootable OS image with the sensor app running as a systemd service.

Build the bootc image:

```bash
cd remoteSensor
./mvnw package -DskipTests
podman build -f src/main/docker/Containerfile.bootc -t remote-sensor-bootc .
```

The bootc image:
- Uses `registry.redhat.io/rhel10/rhel-bootc:latest` as the base
- Installs Java 25 (headless) and firewalld
- Runs the Quarkus app via a systemd unit (`remote-sensor.service`)
- Reads configuration from `/etc/remote-sensor/remote-sensor.env`

### remoteSensor configuration

| Environment variable | Default | Description |
|---|---|---|
| `SENSOR_ID` | `sensor1` | Unique identifier for the sensor |
| `SENSOR_START_LAT` | `38.8977` | Initial latitude |
| `SENSOR_START_LNG` | `-77.0365` | Initial longitude |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Kafka bootstrap address |
| `KAFKA_SSL_TRUSTSTORE_LOCATION` | — | Path to JKS truststore |
| `KAFKA_SSL_TRUSTSTORE_PASSWORD` | `sensorcapass` | Truststore password |

