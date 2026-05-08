package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestStreamElementType;

import demo.redhat.sensorTracking.model.SensorData;

@Path("/sensors")
public class SensorService {

    @Channel("sensor-data")
    Multi<SensorData> stream;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorData> stream() {
        return stream;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<SensorData> streamById(@PathParam("id") String id) {
        return stream.filter(s -> id.equals(s.getId()));
    }
}
