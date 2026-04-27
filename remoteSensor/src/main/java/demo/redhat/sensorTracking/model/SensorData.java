package demo.redhat.sensorTracking.model;

// import jakarta.validation.constraints.DecimalMax;
// import jakarta.validation.constraints.DecimalMin;

public class SensorData {

    private String id;

    // @DecimalMin("-90.0")
    // @DecimalMax("90.0")
    private double lat;

    // @DecimalMin("-180.0")
    // @DecimalMax("180.0")
    private double lng;

    private double alt;
    private double acc;
    private double speed;
    private double heading;

    public SensorData() {}

    public SensorData(String id, double lat, double lng, double alt,
                      double acc, double speed, double heading) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.acc = acc;
        this.speed = speed;
        this.heading = heading;
    }

    public String getId()          { return id; }
    public double getLat()         { return lat; }
    public double getLng()         { return lng; }
    public double getAlt()         { return alt; }
    public double getAcc()         { return acc; }
    public double getSpeed()       { return speed; }
    public double getHeading()     { return heading; }

    public void setId(String id)           { this.id = id; }
    public void setLat(double lat)         { this.lat = lat; }
    public void setLng(double lng)         { this.lng = lng; }
    public void setAlt(double alt)         { this.alt = alt; }
    public void setAcc(double acc)         { this.acc = acc; }
    public void setSpeed(double speed)     { this.speed = speed; }
    public void setHeading(double heading) { this.heading = heading; }

    @Override
    public String toString() {
        return "SensorData{" +
                "id='" + id + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", alt=" + alt +
                ", acc=" + acc +
                ", speed=" + speed +
                ", heading=" + heading +
                '}';
    }
}
