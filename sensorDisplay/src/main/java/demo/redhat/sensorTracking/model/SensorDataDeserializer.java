package demo.redhat.sensorTracking.model;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;


public class SensorDataDeserializer extends ObjectMapperDeserializer<SensorData> {
    public SensorDataDeserializer() {
        super(SensorData.class);
    }

}
