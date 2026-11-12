# sensorTracking

A Quarkus-based GPS sensor tracking demo. A remote sensor publishes simulated GPS telemetry to Kafka over TLS, and a display application consumes the data and renders moving markers on a dark-themed map.

## Project structure

```
sensorTracking/
├── remoteSensor/         # Quarkus producer — generates and publishes GPS data to Kafka
├── sensorDisplay/        # Quarkus consumer — renders sensor positions on a Leaflet map
│   └── deploy/           # Kubernetes/OpenShift manifests (Deployment, Service, Route, ConfigMap, Secret)
├── kafka/                # AMQ Streams / Strimzi Kafka manifests with Kustomize
├── flightctl/            # flightctl Fleet manifest and device examples (edge device management)
├── ansible/              # Automation playbooks (cert management, build, deploy)
├── my-bootc-vm.yaml      # Example KubeVirt VirtualMachine manifest
└── pom.xml               # Parent POM (multi-module)
```

## Prerequisites

- Java 25 and Maven (or use the included `mvnw` wrapper)
- Podman (used for container builds)
- A Kafka broker with TLS enabled (see [Deploying Kafka](#deploying-kafka))
- `oc` CLI for OpenShift deployment
- Ansible for running automation playbooks

## Configuration templates

Several files contain environment-specific values (passwords, hostnames) and are gitignored. Template versions with placeholder values are tracked instead:

| Template | Generated file | Description |
|---|---|---|
| `sensorDisplay/deploy/configmap.yaml.template` | `configmap.yaml` | Kafka bootstrap server address |
| `sensorDisplay/deploy/secret.yaml.template` | `secret.yaml` | Truststore password |
| `flightctl/fleet.yaml.template` | `fleet.yaml` | Fleet config with Kafka and truststore settings |
| `remoteSensor/src/main/bootc/remote-sensor.env.template` | `remote-sensor.env` | Sensor runtime environment |

Copy each `.template` to the target filename and fill in your values before deploying.

## Local development

1. Build both modules:

   ```bash
   ./mvnw package
   ```

2. Set up the Kafka TLS truststore. You can do this manually or use the Ansible playbook (see [Certificate management](#certificate-management)):

   ```bash
   export KAFKA_BOOTSTRAP_SERVERS=<your-kafka-bootstrap>:443
   export KAFKA_SSL_TRUSTSTORE_LOCATION=$(pwd)/mykeystore.jks
   export KAFKA_SSL_TRUSTSTORE_PASSWORD=<your-truststore-password>
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
export KEYSTORE_PASSWORD=<your-truststore-password>
ansible-playbook ansible/fetch-certs.yaml
```

Variables:
| Variable | Default | Description |
|---|---|---|
| `cert_urls` | — | List of `{host, port}` endpoints to fetch certs from |
| `keystore_path` | `../mykeystore.jks` | Path to the output JKS file |
| `keystore_password` | env `KEYSTORE_PASSWORD` (fallback: `changeit`) | Keystore password |

The playbook is idempotent — re-running it skips certificates that are already in the keystore.

## Building and pushing images

### sensorDisplay

```bash
ansible-playbook ansible/build-sensor-display.yaml
```

Builds the Quarkus app, creates a container image with Podman, and pushes to `quay.io/<registry_org>/sensor-display:latest`.

### remoteSensor (container image)

```bash
ansible-playbook ansible/build-remote-sensor.yaml
```

Builds the Quarkus app and pushes to `quay.io/<registry_org>/remote-sensor:latest`. This is the application image used inside the bootc OS image and Quadlet containers.

### remoteSensor (bootc image)

```bash
ansible-playbook ansible/build-remote-sensor-bootc.yaml
```

Builds a RHEL Image Mode (bootc) OS image with the sensor app, flightctl agent, and enrollment certificates baked in. Requires Red Hat registry credentials:

```bash
export REGISTRY_SERVICE_ACCOUNT_ID=<your-service-account>
export REGISTRY_SERVICE_ACCOUNT_TOKEN=<your-token>
```

### remoteSensor (qcow2 disk image + DataSource)

```bash
ansible-playbook ansible/build-remote-sensor-qcow2.yaml
```

Converts the bootc image to a qcow2 disk image using `bootc-image-builder`, packages it as a container image, pushes it to the registry, creates a CDI DataVolume in OpenShift, and creates a DataSource for use as a bootable volume source. The DataSource is labeled with KubeVirt instance type and preference metadata so it appears as a bootable volume in the OpenShift console.

## Deploying sensorDisplay to OpenShift

```bash
ansible-playbook ansible/deploy-sensor-display.yaml
```

Creates the Kafka truststore secret, applies the Kubernetes manifests from `sensorDisplay/deploy/`, restarts the deployment, and waits for rollout.

The deployment manifests include:
- **ConfigMap** — `KAFKA_BOOTSTRAP_SERVERS` and truststore location
- **Secret** — truststore password
- **Deployment** — pulls from Quay with `imagePullPolicy: Always`, mounts the truststore volume, and includes health probes (`/q/health/ready`, `/q/health/live`)
- **Service** — exposes port 8080
- **Route** — TLS edge termination

## Deploying remote sensor VMs to OpenShift

The `ansible/deploy-remote-sensor-vm.yaml` playbook deploys one or more remote sensor VMs on OpenShift using KubeVirt. Each VM clones its root disk from the DataSource created by the qcow2 build playbook, and gets its own Service and Route.

```bash
ansible-playbook ansible/deploy-remote-sensor-vm.yaml
```

Edit the `sensor_vms` list in the playbook to define the VMs to deploy:

```yaml
sensor_vms:
  - name: remote-sensor-01
    sensor_id: sensor-01
  - name: remote-sensor-02
    sensor_id: sensor-02
    start_lat: "39.0997"
    start_lng: "-94.5786"
```

For each entry the playbook creates:
- A **VirtualMachine** with a cloned PVC from the `remote-sensor-boot` DataSource
- A **Service** on port 8080
- A **Route** with TLS edge termination
- A shared **cloud-init Secret** (from `cloud-config.yaml`) for flightctl enrollment

## flightctl fleet management (edge devices)

The `flightctl/` directory contains a Fleet manifest for managing remote sensor devices via [flightctl](https://github.com/flightctl/flightctl). The fleet delivers:

- A Quadlet `.container` file (via the `applications` section) to run the sensor as a rootless Podman container
- An environment file with per-device sensor ID and Kafka connection settings (templated from device labels)
- The Kafka TLS truststore (base64-encoded, decoded at container start)

Apply the fleet configuration:

```bash
ansible-playbook ansible/apply-fleet.yaml
```

This playbook base64-encodes the local truststore and injects it into the fleet manifest before applying it with `flightctl apply`.

Per-device settings are configured via device labels:

| Label | Default | Description |
|---|---|---|
| `sensor-id` | `sensor-unknown` | Unique sensor identifier |
| `sensor-start-lat` | `38.8977` | Initial latitude |
| `sensor-start-lng` | `-77.0365` | Initial longitude |

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
| `SENSOR_ID` | `sensor-1` | Unique identifier for the sensor |
| `SENSOR_START_LAT` | `38.8977` | Initial latitude |
| `SENSOR_START_LNG` | `-77.0365` | Initial longitude |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Kafka bootstrap address |
| `KAFKA_SSL_TRUSTSTORE_LOCATION` | — | Path to JKS truststore |
| `KAFKA_SSL_TRUSTSTORE_PASSWORD` | — | Truststore password |
| `KAFKA_SECURITY_PROTOCOL` | `SSL` | Kafka security protocol |
