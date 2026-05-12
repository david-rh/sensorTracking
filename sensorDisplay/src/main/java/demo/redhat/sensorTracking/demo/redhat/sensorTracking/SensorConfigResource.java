package demo.redhat.sensorTracking.demo.redhat.sensorTracking;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import demo.redhat.sensorTracking.model.SensorConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sensor-configs")
@ApplicationScoped
public class SensorConfigResource {

    private final List<SensorConfig> configs = new CopyOnWriteArrayList<>(List.of(
        new SensorConfig("sensor-1", "Alpha Unit",   "#06c",    38.8977, -77.0365, 45),
        new SensorConfig("sensor-2", "Bravo Unit",   "#c9190b", 38.9072, -77.0490, 140),
        new SensorConfig("sensor-3", "Charlie Unit", "#f0ab00", 38.8890, -77.0200, 220),
        new SensorConfig("sensor-4", "Delta Unit",   "#3e8635", 38.9150, -77.0300, 300)
    ));

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorConfig> list() {
        return configs;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(SensorConfig config) {
        configs.add(config);
        return Response.status(Response.Status.CREATED).entity(config).build();
    }

    @DELETE
    @Path("/{id}")
    public Response remove(@PathParam("id") String id) {
        boolean removed = configs.removeIf(c -> c.getId().equals(id));
        return removed
            ? Response.noContent().build()
            : Response.status(Response.Status.NOT_FOUND).build();
    }
}
