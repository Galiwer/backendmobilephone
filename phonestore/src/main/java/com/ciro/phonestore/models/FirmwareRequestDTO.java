package com.ciro.phonestore.models;

public class FirmwareRequestDTO {
    private String brand;
    private String model;
    private String version;
    private String firmwareLink;
    private String releaseNotes;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFirmwareLink() {
        return firmwareLink;
    }

    public void setFirmwareLink(String firmwareLink) {
        this.firmwareLink = firmwareLink;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
}