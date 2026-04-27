package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;

import demo.redhat.sensorTracking.model.SensorData;

@Path("/sensors")
public class SensorService {

    @Channel("sensor-data")
    Multi<SensorData> stream;


    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<SensorData> stream() {
        return stream.map(s -> new SensorData(s.getId(), s.getLat(), s.getLng(), s.getAlt(), s.getAcc(), s.getSpeed(), s.getHeading()));
    }

}
