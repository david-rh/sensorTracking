package demo.redhat.sensorTracking.model;

public class SensorConfig {

    private String id;
    private String name;
    private String color;
    private double startLat;
    private double startLng;
    private double startHeading;

    public SensorConfig() {}

    public SensorConfig(String id, String name, String color, double startLat, double startLng, double startHeading) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.startLat = startLat;
        this.startLng = startLng;
        this.startHeading = startHeading;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public double getStartLat() { return startLat; }
    public void setStartLat(double startLat) { this.startLat = startLat; }

    public double getStartLng() { return startLng; }
    public void setStartLng(double startLng) { this.startLng = startLng; }

    public double getStartHeading() { return startHeading; }
    public void setStartHeading(double startHeading) { this.startHeading = startHeading; }
}
