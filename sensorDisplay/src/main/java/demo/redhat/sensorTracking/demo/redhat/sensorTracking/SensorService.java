package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.lang.System.Logger.Level;

import org.eclipse.microprofile.reactive.messaging.Channel;

import demo.redhat.sensorTracking.model.SensorData;

@Path("/sensors")
public class SensorService {

    @Channel("sensor-data")
    Multi<SensorData> stream;


    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<SensorData> stream() {

        // FIXME The SensorData object need to be converted to JSON, this conversion isn't happening for some reason

        System.getLogger(SensorService.class.getName()).log(Level.DEBUG, stream);
        return stream.log();
    }

}
