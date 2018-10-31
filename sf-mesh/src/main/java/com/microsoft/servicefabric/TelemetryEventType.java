package com.microsoft.servicefabric;

public enum TelemetryEventType {
    INIT("Java.MavenCLI.Init"),
    ADDSERVICE("Java.MavenCLI.AddService"),
    ADDVOLUME("Java.MavenCLI.AddVolume"),
    DEPLOYLOCAL("Java.MavenCLI.DeployLocal"),
    DEPLOYMESH("Java.MavenCLI.DeployMesh"),
    REMOVE("Java.MavenCLI.Remove"),
    ADDNETWORK("Java.MavenCLI.AddNetwork"),
    ADDGATEWAY("Java.MavenCLI.AddGateway");

    private final String value;

    private TelemetryEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
