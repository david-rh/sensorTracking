package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import demo.redhat.sensorTracking.model.SensorData;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/sensor")
public class SensorResource {

    @Inject
    SensorPublisher sensorPublisher;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SensorData getCurrentData() {
        return sensorPublisher.getSensorData();
    }

    @POST
    @Path("/message")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public SensorData setMessage(String message) {
        sensorPublisher.getSensorData().setMessage(message == null || message.isBlank() ? null : message);
        return sensorPublisher.getSensorData();
    }
}
