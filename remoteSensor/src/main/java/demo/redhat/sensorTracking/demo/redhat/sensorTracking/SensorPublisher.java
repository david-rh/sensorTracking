package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import demo.redhat.sensorTracking.model.SensorData;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SensorPublisher {

    @Outgoing("sensor-data")
    public Multi<SensorData> stream() {
        return Multi.createFrom().ticks().every(java.time.Duration.ofSeconds(1))
                .map(tick -> new SensorData("sensor-" + tick, Math.random() * 180 - 90, Math.random() * 360 - 180, Math.random() * 1000, Math.random() * 10, Math.random() * 100, Math.random() * 360));
    }
        
}
