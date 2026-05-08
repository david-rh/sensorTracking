package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import demo.redhat.sensorTracking.model.SensorData;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SensorPublisher {

    private SensorData sensorData;

    public SensorPublisher() {
        initSensorData();
    }

    private void initSensorData() {
        sensorData = new SensorData("sensor-1", 38.8977, -77.0365, 0.0, 0.0, 0.0, 45);

    }

    @Outgoing("sensor-data")
    public Multi<KafkaRecord<String, SensorData>> stream() {
        return Multi.createFrom().ticks().every(java.time.Duration.ofSeconds(1))
                .map(tick -> processUpdate(tick)).onFailure().retry().indefinitely();
    }

    private KafkaRecord<String, SensorData> processUpdate(Long tick) {

        sensorData.setSpeed(Math.max(0, Math.min(60, sensorData.getSpeed() + (Math.random() - 0.4) * 10)));
        sensorData.setHeading((sensorData.getHeading() + (Math.random() - 0.5) * 20) % 360);
        double distance = sensorData.getSpeed() * 1000 / 3600; // distance in meters per second
        double deltaLat = distance * Math.cos(Math.toRadians(sensorData.getHeading())) / 111320; // convert to degrees
        double deltaLng = distance * Math.sin(Math.toRadians(sensorData.getHeading())) / (111320 * Math.cos(Math.toRadians(sensorData.getLat()))); // convert to degrees
        sensorData.setLat(sensorData.getLat() + deltaLat);
        sensorData.setLng(sensorData.getLng() + deltaLng);
        sensorData.setAcc(2 + Math.random() * 5);
        sensorData.setAlt(100 + Math.random() * 10);

        SensorData updated = new SensorData(sensorData.getId(), sensorData.getLat(), sensorData.getLng(), sensorData.getAlt(), sensorData.getAcc(), sensorData.getSpeed(), sensorData.getHeading());
        return KafkaRecord.of(updated.getId(), updated);
    }
        
}
