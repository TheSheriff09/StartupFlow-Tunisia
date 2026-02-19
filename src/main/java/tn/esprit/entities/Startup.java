package tn.esprit.entities;

public class Startup {
    private int startupID;
    private String name;
    private String description;
    private String sector;

    public Startup() {}

    public Startup(int startupID, String name, String description, String sector) {
        this.startupID = startupID;
        this.name = name;
        this.description = description;
        this.sector = sector;
    }

    public int getStartupID() { return startupID; }
    public void setStartupID(int startupID) { this.startupID = startupID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    @Override
    public String toString() {
        return name + " (ID: " + startupID + ")";
    }
}